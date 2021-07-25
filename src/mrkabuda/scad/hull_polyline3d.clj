(ns mrkabuda.scad.hull_polyline3d
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]))


;;;;; hull-polyline3d ;;;;;

;; (hull-polyline3d [[1 2 3]
;;                  [4 -5 -6]
;;                  [-1 -3 -5]
;;                  [0 0 0]]
;;                  1)

;; (def spiral
;;   (let [r  50
;;         points  (vec (for [a (range 0 180)] 
;;                   [
;;                    (* r (Math/cos (deg2rad (+ -90 a))) (Math/cos (deg2rad a)))
;;                    (* r (Math/cos (deg2rad (+ -90 a))) (Math/sin (deg2rad a)))
;;                    (* r (Math/sin (deg2rad (+ -90 a))))
;;                    ]))]
;;
;;       (for [i  (range 0 7)]
;;         (->> (hull-polyline3d points 2)
;;              (rotate (* (deg2rad 45) i) [0 0 1]) 
;;                           )
;;         )
;;       ))
(defn hull-polyline3d [points thickness]
  (let [
        half-thickness (/ thickness 2)
        leng (count points)
        hull-line3d (fn [index] 
                      (let [point1 (get points (dec index))
                            point2 (get points index)
                            ]
                      (hull
                        (translate point1 (sphere half-thickness))
                        (translate point2 (sphere half-thickness)))))]
  ;; (union 
    (for [index (range 1 leng)]
      (hull-line3d index))
    ;; )
  ))

    
