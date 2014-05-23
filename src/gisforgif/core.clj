(ns gisforgif.core
  (:require [clojure.java.io :as io])
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
     result (BufferedImage. w h BufferedImage/TYPE_INT_ARGB)]
    (.filter atOp frame result)))

(defn -main
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
