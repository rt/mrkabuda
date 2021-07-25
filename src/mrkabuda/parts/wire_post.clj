(ns mrkabuda.parts.wire_post
  (:refer-clojure :exclude [use import])
  (:require [clojure.core.matrix :refer [array matrix mmul]]
            [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [mrkabuda.scad.util :refer :all]
            [unicode-math.core :refer :all]))


(def wire-post-height 7)
(def wire-post-overhang 3.5)
(def wire-post-diameter 2.6)

(defn wire-post [config direction offset mount-size]
   (->> (union (translate [0 (* wire-post-diameter -0.5 direction) 0] (cube wire-post-diameter wire-post-diameter wire-post-height))
               (translate [0 (* wire-post-overhang -0.5 direction) (/ wire-post-height -2)] (cube wire-post-diameter wire-post-overhang wire-post-diameter)))
        (translate [0 (- offset) (+ (/ wire-post-height -2) 3) ])
        (rotate (/ (:col-curvature config) -2) [1 0 0])
        (translate [3 (/ (:mount-height mount-size) -2) 0])))

;; (defn wire-posts [config]
;;   (union
;;      (for [column (range 0 (:ncols config))
;;            row (range 0 (:nrows config))]
;;        (union
;;         (key-place config column row (translate [-5 0 0] (wire-post config 1 0 (:1u mount-size))))
;;         (key-place config column row (translate [0 0 0] (wire-post config -1 6 (:1u mount-size))))
;;         (key-place config column row (translate [5 0 0] (wire-post  config 1 0 (:1u mount-size))))))))

