(ns mrkabuda.scad.line3d
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [mrkabuda.scad.util :refer :all]))


  ;; width = 1 cap_round = true
(defn line [point1 point2 width cap_round]
  (let [
        angle (- 90 (Math/atan (/ (- (get point2 1) (get point1 1)) (- (get point2 0) (get point1 0)))))
        offset_x (* 0.5 width (Math/cos (deg2rad angle)))
        offset_y (* 0.5 width (Math/sin (deg2rad angle)))
        offset1 [(- offset_x) offset_y]
        offset2 [offset_x (- offset_y)]
        ]

    (if cap_round
      (translate point1 (with-fn 24 (circle (/ width 2))))
      (translate point2 (with-fn 24 (circle (/ width 2))))
      )

    (polygon [
              (map + point1 offset1)
              (map + point2 offset1)
              (map + point2 offset2)
              (map + point1 offset2)
              ])))
