(ns mrkabuda.scad.polyline3d
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [mrkabuda.scad.line3d :refer :all]))


(defn polyline-inner [points width]
  (for [i (range 0 (- (count points) 1))]
    (line (get points i) (get points (inc i)) width true)))


