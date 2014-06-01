(ns gisforgif.core
  (:require [clojure.java.io :as io])
  (:use [clojure.core.match :only (match)])
  (:import [org.jcodec.api FrameGrab]
           [org.jcodec.common FileChannelWrapper NIOUtils]
           [javax.imageio ImageIO]
           [java.awt.geom AffineTransform]
           [java.awt.image AffineTransformOp BufferedImage]
           AnimatedGifEncoder)
  (:gen-class))

(defn frame-grab [f]
  (FrameGrab. (NIOUtils/readableFileChannel
                (io/file f))))

(defn encoder [out]
  (doto (AnimatedGifEncoder.)
    (.start out)
    (.setDelay 60)
    (.setRepeat 0)))

(defn write-frame-to-file [fg i]
  (ImageIO/write
    (.getFrame (.seek fg i))
    "png"
    (io/file (str "out" i ".png"))))

(defn scale-frame [frame scale]
  (let
    [at (doto (AffineTransform.) (.scale scale scale))
     atOp (AffineTransformOp. at AffineTransformOp/TYPE_BILINEAR)
     w (* (.getWidth frame) scale)
     h (* (.getHeight frame) scale)
     scaled (BufferedImage. w h BufferedImage/TYPE_INT_ARGB)]
    (.filter atOp frame scaled)))

(defn make-gif [filein fileout first-frame n-frames]
  (let
    [fg (frame-grab filein)
     e (encoder fileout)]
    (println (str (type first-frame) " " (type n-frames)))
    (doseq [i (range first-frame (+ first-frame n-frames))]
      (let [frame
            (try
              (.getFrame (.seek fg i))
              (catch Exception e nil))]
        (if frame
          (do
            (println (str "good frame " i))
            (.addFrame e (scale-frame frame 0.5)))
          (println (str "bad frame " i)))))
    (.finish e)))

(defn -main
  "Me take movie. Me grab frames. Me make animated gif"
  [filein fileout first-frame n-frames & args]
  (make-gif
    filein
    fileout
    (Integer/parseInt first-frame)
    (Integer/parseInt n-frames)))

(defn old-main
  "Me take movie. Me grab frames. Me make animated gif"
  [& args]
  (let
    [fg (frame-grab "MVI_1503.MOV")
     e (encoder "out.gif")]
     (doseq [i (range 40 50)]
       (.addFrame e (scale-frame
                      (.getFrame (.seek fg i))
                      0.5)))
    (.finish e))) 


; scratchpad ------------------------------------

(defn get-frame-n [fg i]
  (.getFrame (.seek fg i)))

(defn get-n-frames [fg init n]
  (loop [frames []]
    (let [x (count frames)]
      (println (str x " frames"))
      (if (= x n) frames
        (let [next-frame
              (try
                (get-frame-n fg (+ init x))
                (catch Exception e nil))]
          (if (nil? next-frame) (recur frames)
            (recur (conj frames next-frame))))))))

(defn very-functional-main
  "Me take movie. Me grab frames. Me make animated gif"
  [& args]
  (let
    [fg (frame-grab "MVI_1503.MOV")
     e (encoder "out.gif")]
    (doseq [frame
            (map #(scale-frame % 0.5)
                 (get-n-frames fg 40 10))]
      (println "adding frame")
      (.addFrame e frame))
    (.finish e)))


(defn foo []
  (doseq [n (range 1 101)]
    (println
      (match [(mod n 3) (mod n 5)]
            [0 0] "FizzBuzz"
            [0 _] "Fizz"
            [_ 0] "Buzz"
            :else n))))

(defn do-m# [m]
 (condp > m
    1  "one" ; equiv to (if (> 1 m) "one")
    2  "two"
    5  "five" 
    10 "ten"
    20 "twenty"
    40 "fourty"))









