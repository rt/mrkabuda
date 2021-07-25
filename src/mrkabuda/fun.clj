(ns mrkabuda.fun
  (:refer-clojure :exclude [use import])
  (:require 
            [clojure.core.matrix :refer [array matrix mmul]]
            [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [mrkabuda.scad.util :refer :all]
            [mrkabuda.scad.function_grapher :refer :all]
            [mrkabuda.scad.line3d :refer :all]
            [mrkabuda.scad.polyline3d :refer :all]
            [mrkabuda.scad.hull_polyline3d :refer :all]
            [unicode-math.core :refer :all]))
  ;; (:gen-class))


(def cube-points [
                  [  0  0  0 ]  ;;0
                  [ 10  0  0 ]  ;;1
                  [ 10  7  0 ]  ;;2
                  [  0  7  0 ]  ;;3
                  [  0  0  5 ]  ;;4
                  [ 10  0  5 ]  ;;5
                  [ 10  7  7 ]  ;;6
                  [  0  7  5 ] ;;7
                  ])

(def cube-faces [
                 [0 1 2 3]   ;; bottom
                 [4 5 1 0]   ;; front
                 [7 6 5 4]   ;; top
                 [5 6 2 1]   ;; right
                 [6 7 3 2]   ;; back
                 [7 4 0 3] ;; left
  ])

;; function _combi(n, k) =
;;     let(  
;;         bi_coef = [      
;;                [1],     // n = 0: for padding
;;               [1,1],    // n = 1: for Linear curves, how about drawing a line directly?
;;              [1,2,1],   // n = 2: for Quadratic curves
;;             [1,3,3,1]   // n = 3: for Cubic Bézier curves
;;         ]  
;;     )
;;     n < len(bi_coef) ? bi_coef[n][k] : (
;;         k == 0 ? 1 : (_combi(n, k - 1) * (n - k + 1) / k)
;;     );
;; function bezier_curve_coordinate(t, pn, n, i = 0) = 
;;     i == n + 1 ? 0 : 
;;         (_combi(n, i) * pn[i] * pow(1 - t, n - i) * pow(t, i) + 
;;             bezier_curve_coordinate(t, pn, n, i + 1));

(defn bezier-coordinate [t n0 n1 n2 n3]
                   (let [ ]
                     (+ 
                       (* n0 (Math/pow (- 1 t) 3)) 
                       (* 3 n1 t (Math/pow (- 1 t) 2)) 
                       (* 3 n2 (Math/pow t 2) (- 1 t)) 
                       (* n3 (Math/pow t 3))
                       )
                     )
                   )

(defn bezier-point [t p0 p1 p2 p3]
    (vector
        (bezier-coordinate t (get p0 0) (get p1 0) (get p2 0) (get p3 0))
        (bezier-coordinate t (get p0 1) (get p1 1) (get p2 1) (get p3 1))
        (bezier-coordinate t (get p0 2) (get p1 2) (get p2 2) (get p3 2))))

(defn bezier-curve 
  "https://openhome.cc/eGossip/OpenSCAD/BezierCurve.html"
  [t_step p0 p1 p2 p3]
  (vec 
    (for [t (range 0 (+ 1 t_step) t_step)]
      (bezier-point t p0 p1 p2 p3))))


(def polyline
  (let [
        t_step 0.05
        p0 [0 0 0]
        p1 [40 60 0]
        p2 [50 90 0]
        p3 [0 200 0]
        ]
  (polyline-inner (bezier-curve t_step p0 p1 p2 p3) 2)))

(defn outer-shell [shape1 shape2]
  (difference
    (union 
      (translate [5 0 0] (sphere 10))
      (translate [-5 0 0] (sphere 10))
      )
    (intersection 
      (translate [5 0 0] (sphere 10))
      (translate [-5 0 0] (sphere 10))
      )
    (translate [0 0 -5] (cube 100 100 10)) ;; shave bottom
    )
  )

(def fun
   ;; (line [0 0 0] [40 60 0] 0.1 true)
  ;; (polyline-inner [[0 0 0] [40 60 0] [-50 90 0] [0 200 0]] 1)
  ;; polyline
  ;; fun-function
  ;; (extrude-linear {:height 10} (circle 5))

  ;; (polyhedron cube-points cube-faces)
;; (hull-polyline3d [[1 2 3]
;;                  [4 -5 -6]
;;                  [-1 -3 -5]
;;                  [0 0 0]]
;;                  1)
  )


(spit "things/fun.scad" (write-scad fun))

(defn -main [dum] 1)  ; dummy to make it easier to batch
