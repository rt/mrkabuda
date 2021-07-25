(ns mrkabuda.keycaps.sa
  (:refer-clojure :exclude [use import])
  (:require [clojure.core.matrix :refer [array matrix mmul]]
            [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [mrkabuda.scad.util :refer :all]
            [unicode-math.core :refer :all]))

;;;;;;;;;;;;;;;;
;; SA Keycaps ;;
;;;;;;;;;;;;;;;;

(def sa-length 18.25)
(def sa-double-length 37.5)
(defn sa-cap [size plate-thicknesss] 
  (let
    [cap {
          :1u (let [bl2 (/ 18.5 2)
                    m (/ 17 2)
                    key-cap (hull (->> (polygon [[bl2 bl2] [bl2 (- bl2)] [(- bl2) (- bl2)] [(- bl2) bl2]])
                                       (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                       (translate [0 0 0.05]))
                                  (->> (polygon [[m m] [m (- m)] [(- m) (- m)] [(- m) m]])
                                       (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                       (translate [0 0 6]))
                                  (->> (polygon [[6 6] [6 -6] [-6 -6] [-6 6]])
                                       (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                       (translate [0 0 12])))]
                (->> key-cap
                     (translate [0 0 (+ 5 plate-thicknesss)])
                     (color [220/255 163/255 163/255 1])))
          :2u (let [bl2 (/ sa-double-length 2)
                    bw2 (/ 18.25 2)
                    key-cap (hull (->> (polygon [[bw2 bl2] [bw2 (- bl2)] [(- bw2) (- bl2)] [(- bw2) bl2]])
                                       (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                       (translate [0 0 0.05]))
                                  (->> (polygon [[6 16] [6 -16] [-6 -16] [-6 16]])
                                       (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                       (translate [0 0 12])))]
                (->> key-cap
                     (translate [0 0 (+ 5 plate-thicknesss)])
                     (color [127/255 159/255 127/255 1])))
          :1.5u (let [bl2 (/ 18.25 2)
                      bw2 (/ 28 2)
                      key-cap (hull (->> (polygon [[bw2 bl2] [bw2 (- bl2)] [(- bw2) (- bl2)] [(- bw2) bl2]])
                                         (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                         (translate [0 0 0.05]))
                                    (->> (polygon [[11 6] [-11 6] [-11 -6] [11 -6]])
                                         (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                         (translate [0 0 12])))]
                  (->> key-cap
                       (translate [0 0 (+ 5 plate-thicknesss)])
                       (color [240/255 223/255 175/255 1])))}]
    (cap size)
    ))

(def stem-diameter 5.5)
(def stem-insert-length 4)
(def stem-insert-width 1.26)
(def stem-bottom-z-offset 1.3) ; stem goes from top of key but not to the cap base

(def stem-stabilizer-distance-2x 12) ; 2u, 2.25u, 2.75u one on each side of the main stem
(def stem-stabilizer-distance-6 49) 
(def stem-stabilizer-distance-6.25 50) 
(def stem-stabilizer-distance-6.5 52.5) 

;; (def stem-brace-width 1) this is the cross brace inside the cap that supports the stem (not sure how to make it...)

(defn keycap
  "Given all the dimensions of a keycap, you should get it"
  [
   bottom-length ;18
   bottom-width     ;18
   top-length
   top-width
   top-offset       ; offset from bottom xy
   height-front
   height-back
   cap-thickness
   bottom-corner-radius   ; if this is gonna be a post, will need the angle to rotate ...
   top-corner-radius      ; top corners often look rounder/smoother (not sure if this is due to the surface-radius too)
   x-surface-radius         
   y-surface-radius        
   x-side-surface-radius
   y-side-surface-radius
   ]
  (str "hello")
 )
