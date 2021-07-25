(ns mrkabuda.parts.screw_insert
  (:refer-clojure :exclude [use import])
  (:require [clojure.core.matrix :refer [array matrix mmul]]
            [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [mrkabuda.scad.util :refer :all]
            [unicode-math.core :refer :all]))

(def screw-insert-height 3.8)
(def screw-insert-bottom-radius (/ 5.31 2))
(def screw-insert-top-radius (/ 5.1 2))

(def screw-hole-radius 1.7) ; radius of the hole that the screw will go thru (botton of the case)

(defn screw-insert-shape [bottom-radius top-radius height]
  (translate [0 0 (/ height 2)]
   (union (cylinder [bottom-radius top-radius] height)
          (translate [0 0 (/ height 2)] (sphere top-radius)))))

(defn screw-insert-round [radius]
  (difference
    (screw-insert-shape (+ screw-insert-bottom-radius 1.6) (+ screw-insert-top-radius 1.6) (+ screw-insert-height 1.5))
    (screw-insert-shape  screw-insert-bottom-radius screw-insert-top-radius screw-insert-height)
    ))

(defn screw-insert-cube [size]
  (difference
    (translate [0 0 (/ size 2)] (cube size size size))
    (screw-insert-shape  screw-insert-bottom-radius screw-insert-top-radius screw-insert-height)
    ))

