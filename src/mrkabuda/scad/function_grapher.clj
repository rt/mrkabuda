(ns mrkabuda.scad.function_grapher
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]))


(def fun-function
  (let [ 
        min-value -3
        max-value 3
        resolution 0.5
        thickness 1
        exp (fn [x n] (reduce * (repeat n x)))
        f (fn [x y] (- (/ (exp y 2) (exp 2 2)) (/ (exp x 2) (exp 2 2))))
        ]

    ;; (apply union
           (for [
                 x (range min-value max-value resolution)
                 y (range min-value max-value resolution)
                 ]
             (translate [x y (f x y)]
                        (cube resolution resolution resolution)
                        ;; (extrude-linear thickness (square resolution resolution))
                        ))
           ;; )
    
    )
  )


;; (defn function_grapher [points thickness style = "FACES" slicing = "SLASH"] 
(defn function_grapher [points thickness style slicing] 
  (let [
        rows (count points)
        columns (count (get points 0))
        yi_range (range 0 (- rows 2))
        xi_range (range 0 (- columns 2))
        ;; Increasing $fn will be slow when you use "LINES", "HULL_FACES" or "HULL_LINES".
        xy_to_index (fn [x y columns] (+ (* y columns) x))
        top_pts (vec (for [ row_pts points pt row_pts ] pt))
        base_pts (vec (for [ pt top_pts] [(get pt 0) (get pt 1) (- (get pt 2) thickness)]))
        leng_pts (count top_pts)

        top_tri_faces1  (vec (for [yi yi_range xi xi_range]
                               (vector
                                 (xy_to_index xi yi columns)
                                 (xy_to_index (+ xi 1) (+ yi 1) columns)
                                 (xy_to_index (+ xi 1) yi columns)
                                 )
                               )) 

        top_tri_faces2 (vec (for [yi yi_range xi xi_range]
                              (vector
                                (xy_to_index xi yi columns)
                                (xy_to_index xi (+ yi 1) columns)
                                (xy_to_index (+ xi 1) (+ yi 1) columns)
                                )
                              ))

        offset_v [leng_pts leng_pts leng_pts]
        base_tri_faces1 (vec (for [face top_tri_faces1]
                               (map + (reverse face) offset_v)
                               ))

        base_tri_faces2 (vec (for [face top_tri_faces2]
                               (map + (reverse face) offset_v)
                               ))

        side_faces1 (vec (for [xi xi_range]
                           (let [
                                 idx1 (xy_to_index xi 0 columns)
                                 idx2 (xy_to_index (+ xi 1) 0 columns)
                                 ]
                             (vector idx1 idx2 (+ idx2 leng_pts) (+ idx1 leng_pts))
                             )))

        side_faces2 (vec (for [yi yi_range]
                           (let [
                                 xi (- columns 1)
                                 idx1 (xy_to_index xi yi columns)
                                 idx2 (xy_to_index xi (+ yi 1) columns)
                                 ]
                             (vector idx1 idx2 (+ idx2 leng_pts) (+ idx1 leng_pts))
                             )))

        side_faces3 (vec (for [xi xi_range]
                           (let [
                                 yi  (- rows 1)
                                 idx1  (xy_to_index xi yi columns) 
                                 idx2  (xy_to_index (+ xi 1) yi columns)
                                 ]
                             (vector idx2 idx1 (+ idx1 leng_pts) (+ idx2 leng_pts))
                             )))

        side_faces4 (vec (for [yi yi_range]
                           (let [
                                 idx1 (xy_to_index 0 yi columns)
                                 idx2 (xy_to_index 0 (+ yi 1) columns)
                                 ]
                             (vector idx2 idx1 (+ idx1 leng_pts) (+ idx2 leng_pts))
                             )))               

        pts (concat top_pts base_pts)
        face_idxs (concat
                    top_tri_faces1 top_tri_faces2
                    base_tri_faces1 base_tri_faces2 
                    side_faces1 
                    side_faces2 
                    side_faces3 
                    side_faces4
                    )
        ]

    (polyhedron pts face_idxs)

    ))



