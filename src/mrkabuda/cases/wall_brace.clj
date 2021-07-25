(ns mrkabuda.cases.wall_brace
  (:refer-clojure :exclude [use import])
  (:require [clojure.core.matrix :refer [array matrix mmul]]
            [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [mrkabuda.scad.util :refer :all]
            [mrkabuda.parts.switch_mount :refer :all]
            [unicode-math.core :refer :all]))


;; should be in config
(def wall-z-offset -15)                 ; length of the first downward-sloping part of the wall (negative)
(def wall-xy-offset 8)                  ; offset in the x and/or y direction for the first downward-sloping part of the wall (negative)
(def wall-thickness 2)                  ; wall thickness parameter; originally 5

(defn- bottom [height p]
  (->> (project p)
       (extrude-linear {:height height :twist 0 :convexity 0})
       (translate [0 0 (- (/ height 2) 10)])))

(defn- bottom-hull [& p]
  (hull p (bottom 0.001 p)))

(defn wall-brace [plate-thickness pos1 dx1 dy1 pos2 dx2 dy2 ]
  (let [
        post (plate-post plate-thickness) ;; why can I use this same instance, or do I get one for each instance
        pos1-top (first pos1)
        pos1-bot (last pos1)
        pos2-top (first pos2)
        pos2-bot (last pos2)
        wall-locate1 (fn [dx dy] [(* dx wall-thickness) (* dy wall-thickness) -1])
        wall-locate2 (fn [dx dy] [(* dx wall-xy-offset) (* dy wall-xy-offset) wall-z-offset])
        wall-locate3 (fn [dx dy] [(* dx (+ wall-xy-offset wall-thickness)) (* dy (+ wall-xy-offset wall-thickness)) wall-z-offset])
        ]
  (union
    (hull
      (translate pos1-top (sphere 0.1))
      (translate pos1-bot post)
      (translate pos1-bot (translate (wall-locate1 dx1 dy1) post))
      (translate pos1-bot (translate (wall-locate2 dx1 dy1) post))
      (translate pos1-bot (translate (wall-locate3 dx1 dy1) post))
      (translate pos2-top (sphere 0.1))
      (translate pos2-bot post)
      (translate pos2-bot (translate (wall-locate1 dx2 dy2) post))
      (translate pos2-bot (translate (wall-locate2 dx2 dy2) post))
      (translate pos2-bot (translate (wall-locate3 dx2 dy2) post))
      )
    (bottom-hull
      (translate pos1-bot (translate (wall-locate2 dx1 dy1) post))
      (translate pos1-bot (translate (wall-locate3 dx1 dy1) post))
      (translate pos2-bot (translate (wall-locate2 dx2 dy2) post))
      (translate pos2-bot (translate (wall-locate3 dx2 dy2) post))
    )
      )))

(defn top-wall 
  "dx dy represent direction"
  [plate-thickness points-top dx dy]
  (let [
        topbot-points (first points-top)
        points-rest (rest points-top)
        topbot-next-points (first points-rest)
        ]
    (union
      (wall-brace plate-thickness (vec topbot-points) dx dy (vec topbot-next-points) dx dy)
      (when (> (count points-rest) 1) (top-wall plate-thickness points-rest dx dy))
      )
    ))

(defn wall-back [plate-thickness plate-points]
    (top-wall plate-thickness (first plate-points) 0 1))

(defn wall-right [plate-thickness plate-points]
    (top-wall plate-thickness (reduce #(conj %1 (last %2)) [] plate-points) 1 0))

(defn wall-front [plate-thickness plate-points]
  (top-wall plate-thickness (last plate-points) 0 -1))

(defn wall-left [plate-thickness plate-points]
  (top-wall plate-thickness (reduce #(conj %1 (first %2)) [] plate-points) -1 0))

(defn wall-corner [pos plate-thickness plate-points]
  (case pos
    :tr (wall-brace plate-thickness (last (first plate-points)) 0 1  (last (first plate-points)) 1 0)
    :br (wall-brace plate-thickness (last (last plate-points)) 0 -1  (last (last plate-points)) 1 0)
    :bl (wall-brace plate-thickness (first (last plate-points)) -1 0  (first (last plate-points)) 0 -1)
    :tl (wall-brace plate-thickness (first (first plate-points)) -1 0  (first (first plate-points)) 0 1)
    ))

(defn case-walls 
  "All surrounding walls"
  [plate-thickness plate-points ]
  (union
    (wall-back plate-thickness plate-points)
    (wall-corner :tr plate-thickness plate-points)
    (wall-right plate-thickness plate-points)
    (wall-corner :br plate-thickness plate-points)
    (wall-front plate-thickness plate-points)
    (wall-corner :bl plate-thickness plate-points)
    (wall-left plate-thickness plate-points)
    (wall-corner :tl plate-thickness plate-points)
    ))
