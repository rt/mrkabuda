(ns mrkabuda.cases.panel
  (:refer-clojure :exclude [use import])
  (:require 
            [clojure.core.matrix :refer [array matrix mmul]]
            [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [mrkabuda.scad.util :refer :all]
            [unicode-math.core :refer :all]))
  ;; (:gen-class))

;; Cases/Covers with rounded edges
;; Should provide inner/middle/outer shell points
;; Middle is where the center of spheres are

;; Concept
;; Single case, fit to params, unibody (or bottom mount plate version)
;; Implementer will tilt (usually 3-7% so that will be reflected in the walls)
;; If you didn't want tilted walls you would work off of plate-holder only (hull/shell your own)
;; Implementer will also raise to height desired and cut off the bottom
;; - length, width, height
;; - tilt
;; - plate-offset (high-profile vs low-profile)

(defn- cover-corner
  [radius slice-vecs]
  (let [
        cube-size (* radius 2)
        slice-vec1 (map * (first slice-vecs) [radius radius radius])
        slice-vec2 (map * (second slice-vecs) [radius radius radius])
        ]
  (difference
    (sphere radius)
    ;; (translate [0 0 (- radius)] (cube 10 10 10)) ;; shave bottom
    (translate slice-vec1 (cube cube-size cube-size cube-size)) ;; shave y
    (translate slice-vec2 (cube cube-size cube-size cube-size)) ;; shave y
    )
  ))

(defn- plate-holder-corner
  [pos radius length width]
  (let [
        slice-vecs (case pos 
                :tl [[1 0 0] [0 -1 0]]
                :tr [[-1 0 0] [0 -1 0]]
                :bl [[1 0 0] [0 1 0]]
                :br [[-1 0 0] [0 1 0]]
                )
        ]
  (translate (map * [(- (/ length 2)) (- (/ width 2)) 0] (mapv + (first slice-vecs) (second slice-vecs))) (cover-corner radius slice-vecs))))

(defn line [pair radius]
  (hull
    (translate (vec (first pair)) (sphere radius))
    (translate (vec (second pair)) (sphere radius))
    ))

(defn panel [points radius]
  "Uses lines ..."
  (hull
    (line (vec (first points)) radius)
    (line (vec (second points)) radius)
    ))

(defn panel-positions [length width radius]
  [
   [[(/ length -2) (/ width 2) radius] [(/ length 2) (/ width 2) radius]]
   [[(/ length -2) (/ width -2) radius] [(/ length 2) (/ width -2) radius]]
   ])
  
(defn translate-panel [translation points]
  (for [row points]
    (vector
      (mapv + translation (first row))
      (mapv + translation (second row))
      )))

(defn tilt-panel-points [angle points]
  (for [row points]
    (vector
      (rotate-around-x angle (first row))
      (rotate-around-x angle (second row))
      )))

(defn plate-holder [length width radius]
  "
  This uses cylinders ...
  - length : from cylinder center-to-center (actual is length + 2*r)
   - width : from cylinder center-to-center
   - radius : cylinder radius but also dictates thickness (radius * 2)
  "
  (translate [0 0 radius] 
             (hull
               (translate [(/ length 2) 0 0] (rotate (deg2rad 90) [1 0 0] (cylinder radius width)))
               (translate [(/ length -2) 0 0] (rotate (deg2rad 90) [1 0 0] (cylinder radius width)))
               (translate [0 (/ width 2) 0] (rotate (deg2rad 90) [0 1 0] (cylinder radius length)))
               (translate [0 (/ width -2) 0] (rotate (deg2rad 90) [0 1 0] (cylinder radius length)))
               (map #(plate-holder-corner % radius length width) [:tl :tr :bl :br])
               )))

;; (defn- shell [length width height radius thickness direction]
;;   (let
;;     [
;;      outer-plate (plate-holder length width radius) 
;;      inner-plate (plate-holder (- length (* thickness 2)) (- width (* thickness 2)) radius)
;;      solidify (fn [shape] (hull
;;                    shape
;;                    (translate [0 0 (- height)] shape)))
;;      ]
;;   (difference
;;     (solidify outer-plate)
;;     (translate [0 0 (* direction thickness)] (solidify inner-plate) )
;;   )))

(defn- shell [length width height radius thickness direction]
  (let
    [
     outer-plate (panel (panel-positions length width radius) radius) 
     inner-plate (panel (panel-positions (- length (* thickness 2)) (- width (* thickness 2)) radius) radius)
     solidify (fn [shape] (hull
                   shape
                   (translate [0 0 (- height)] shape)))
     ]
  (difference
    (solidify outer-plate)
    (translate [0 0 (* direction thickness)] (solidify inner-plate) )
  )))

(defn case-top [length width height radius thickness]
  (let [
        slice-cube (translate [0 0 (/ height -2)] (cube 350 350 height))
        ]
  (difference
    (translate [0 0 (- height radius)] (shell length width height radius thickness -1))
     slice-cube)))

(defn case-bottom [length width height radius thickness]
  (let [
        slice-cube (translate [0 0 (/ height 2)] (cube 350 350 height))
        ]
  (difference
    (translate [0 0 height] (shell length width height radius thickness 1))
    (translate [0 0 height] slice-cube))))

(defn make-walls [radius top-panel-points]
  (let [
          ;; points:back-panel
        bottom-points (mapv (fn [row] (mapv #(mapv * [1 1 0] %) row)) top-panel-points)
        back-panel-points (vector (vec (first top-panel-points)) (vec (first bottom-points)))
        front-panel-points (vector (vec (second top-panel-points)) (vec (second bottom-points)))
        left-panel-points (vector (mapv #(first %) top-panel-points) (mapv #(first %) bottom-points))
        right-panel-points (vector (mapv #(second %) top-panel-points) (mapv #(second %) bottom-points))
        ]
    (union
        ;; physical:back-panel
        (panel back-panel-points radius)
        (panel front-panel-points radius)
        (panel left-panel-points radius)
        (panel right-panel-points radius)
      ))
)

(defn make-base [height radius top-panel-points]
  (let [
        bottom-points (mapv (fn [row] (mapv #(mapv * [1 1 0] %) row)) top-panel-points)
        bottom-panel (panel bottom-points radius)
        ]
  (difference
      (hull
        bottom-panel
        (translate [0 0 20] bottom-panel)
        )
      (translate [0 0 -20] (cube 350 350 40))
      (translate [0 0 (+ height 20)] (cube 350 350 40))
      )
  ))

(defn basic-plate [length width thickness]
  "Usually for making a plate (as opposed to a unibody)"
  (str "blah")
  )

(defn polygon-plate [points thickness]
  (str "blah")
  )

(spit "things/fun.scad" (write-scad (case-bottom 300 100 10 3 3)))
;; (spit "things/fun.scad" (write-scad (case-top 300 100 10 3 3)))

