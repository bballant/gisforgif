(ns gisforgif.core
  (:require [clojure.java.io :as io])
  (:import [org.jcodec.api FrameGrab]
           [org.jcodec.common FileChannelWrapper NIOUtils]
           [javax.imageio ImageIO])
  (:gen-class))

(defn frame-grab [f]
  (FrameGrab. (NIOUtils/readableFileChannel
                (io/file f))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let
    [fg (frame-grab "MVI_1503.MOV")]
     (doseq [i (range 40 50)]
       (ImageIO/write
         (.getFrame (.seek fg i))
         "png"
         (io/file (str "out" i ".png"))))))
