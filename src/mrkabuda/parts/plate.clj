(ns mrkabuda.parts.plate
  (:refer-clojure :exclude [use import])
  (:require [clojure.core.matrix :refer [array matrix mmul]]
            [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [mrkabuda.scad.util :refer :all]
            [mrkabuda.parts.switch_mount :refer :all]
            [mrkabuda.parts.wire_post :refer :all]
            [mrkabuda.parts.plate_utils :refer :all]
            [unicode-math.core :refer :all]))

;; standard
;; - each key place relative to previous key
;; - in-place rotation optional
;; - offsets optional

(defn- positioning-standard 
  "Calculate the absolute position with row and column
  - Does not look at angle or offset"
  [translate-fn config rotate-x-fn rotate-y-fn rotate-z-fn column row shape]
  (let [
        current-key-config (get-in config [:keys row column])
        current-key-size (:key-size current-key-config)
        current-key-height (+ (:mount-height (mount-size current-key-size)) (:extra-height config))
        current-key-width (+ (:mount-width (mount-size current-key-size)) (:extra-width config))
        get-column (fn [column] (mapv #(nth % column) (:keys config)))
        get-row (fn [row] (nth (:keys config) row))
        current-row (get-row row)
        get-y (fn [row] (* row current-key-height))
        y (+ (get-y row) (/ current-key-height 2))
        get-x (fn [row column] (reduce + 0
                  (for [
                        col (range 0 column)
                        :let [key-size (get-in (get-row row) [col :key-size])]
                        ]
                    (+ (:mount-width (mount-size key-size)) (:extra-width config)))))
        x (+ (get-x row column) (/ current-key-width 2))
        global-position (map + [
                                0 ;(- (get-x (:centerrow config) (:centercol config)))
                                0 ;(get-y (:centerrow config))
                                0
                                ] (vector x (- y) 0))
        ]
    (->> shape
         (rotate-x-fn (deg2rad  (get-in current-key-config [:angle-offset :x])) )
         (rotate-y-fn (deg2rad (get-in current-key-config [:angle-offset :y])) )
         (rotate-z-fn (deg2rad  (get-in current-key-config [:angle-offset :z])) )
         (translate-fn (map + global-position (:offset current-key-config)))
         )))

(defn- positioning-ortho
  "Ortho layout, no angles, must be matrix"
  [translate-fn config rotate-x-fn rotate-y-fn rotate-z-fn column row shape]
  (let [
        current-key-config (get-in config [:keys row column])
        current-key-size (:key-size current-key-config)
        current-key-height (+ (:mount-height (mount-size current-key-size)) (:extra-height config))
        current-key-width (+ (:mount-width (mount-size current-key-size)) (:extra-width config))
        get-column (fn [column] (mapv #(nth % column) (:keys config)))
        get-row (fn [row] (nth (:keys config) row))
        current-row (get-row row)
        key-spacing-height (fn 
                             [key-config] 
                             (if (:zero-height key-config) 
                               0 
                               (+ (:mount-height (mount-size (:key-size key-config))) (:extra-height config))))
        get-y (fn [row column] (reduce + 0
                  (for [ 
                        r (range 0 row)
                        :let [ key-config (nth (get-column column) r) ]
                                 ]
                    (key-spacing-height key-config))))
        y (+ (get-y row column) (/ current-key-height 2))
        key-spacing-width (fn 
                             [key-config] 
                             (if (:zero-width key-config) 
                               0 
                               (+ (:mount-width (mount-size (:key-size key-config))) (:extra-width config))))
        get-x (fn [row column] (reduce + 0
                  (for [
                        col (range 0 column)
                        :let [
                              key-config (nth (get-row row) col)
                              key-size (get-in key-config [:key-size])
                              ]
                        ]
                    (key-spacing-width key-config))))
        x (+ (get-x row column) (/ current-key-width 2))
        global-position (map + [
                                0 ;(- (get-x (:centerrow config) (:centercol config)))
                                0 ;(get-y (:centerrow config))
                                0
                                ] (vector x (- y) 0))
        ]
    (->> shape
         (rotate-x-fn (deg2rad  (get-in current-key-config [:angle-offset :x])) )
         (rotate-y-fn (deg2rad (get-in current-key-config [:angle-offset :y])) )
         (rotate-z-fn (deg2rad  (get-in current-key-config [:angle-offset :z])) )
         (translate-fn (map + global-position (:offset current-key-config)))
         )))

(defn- positioning-ergo 
  "based on 1u mount-size and sa"
  [translate-fn config rotate-x-fn rotate-y-fn rotate-z-fn column row shape]
  (let [
        config-keys (:keys config)
        key-config (get-in config [:keys row column])
        key-size (mount-size (:key-size key-config))
        cap-top-height (+ (:plate-thickness config) (:sa-profile-key-height config))
        row-radius (+ (/ (/ (+ (:mount-height key-size) (:extra-height config)) 2)
                         (Math/sin (/ (:col-curvature config) 2)))
                      cap-top-height)
        column-radius (+ (/ (/ (+ (:mount-width key-size) (:extra-width config)) 2)
                            (Math/sin (/ (:row-curvature config) 2)))
                         cap-top-height)
        placed-shape (->> shape
                          (translate-fn [0 0 (- row-radius)])
                          (rotate-x-fn  (* (:col-curvature config) (- (:centerrow config) row)))
                          (translate-fn [0 0 row-radius])
                          (translate-fn [0 0 (- column-radius)])
                          (rotate-y-fn  (* (:row-curvature config) (- (:centercol config) column)))
                          (translate-fn [0 0 column-radius])
                          (translate-fn (:offset key-config))
                          )
        ]
    (->> (case (:column-style config)
           ;; :gmk placed-shape-gmk
           placed-shape)
         )))

(defn- key-place [config column row shape]
  ((case (:plate-type config) 
     :ergo positioning-ergo 
     :standard positioning-standard
     :ortho positioning-ortho) translate config
    (fn [angle obj] (rotate angle [1 0 0] obj))
    (fn [angle obj] (rotate angle [0 1 0] obj))
    (fn [angle obj] (rotate angle [0 0 1] obj))
    column row shape))

(defn key-position 
  "Get position based on column row"
  [config column row rel-pos]
  (let []
    ;; uses + as translate-fn to add rel-pos to returned position
  ((case (:plate-type config) 
     :ergo positioning-ergo 
     :standard positioning-standard
     :ortho positioning-ortho) 
     (partial map +) config rotate-around-x rotate-around-y rotate-around-z column row rel-pos)))

(defn switch-hole-positions
  "Get all hole positions tl, tr, bl, br. Optionally can add offset (ie. post-adj)"
  ([config]
   (switch-hole-positions config 0))
  ([config offset]
  (let [ config-keys (:keys config) ]
    (for [
          row (range 0 (count config-keys))
          y-offset [1 -1]
          ]
      (for [
            column (range 0 (count (nth (:keys config) row)))
            x-offset [-1 1]
            :let [ key-config (get-in config-keys [row column]) ]
            :when (and (not (:zero-width key-config)) (not (:zero-height key-config))) 
            ]
       (let [
             key-size (mount-size (:key-size key-config))
             row-offset (* y-offset (- (/ (:mount-height key-size) 2) offset))
             col-offset (* x-offset (- (/ (:mount-width key-size) 2) offset))
             ]
        (vector
          (key-position config column row [col-offset row-offset (:plate-thickness config)])
          (key-position config column row [col-offset row-offset 0])
        )))))))

(defn- key-holes
  "Get your key holes"
  [config]
  (let [
        config-keys (:keys config)
        switches (for [
                       row (range 0 (count config-keys))
                       column (range 0 (count (nth config-keys row)))
                       :let [ key-config (get-in config-keys [row column]) ]
                       :when (and (not (:zero-width key-config)) (not (:zero-height key-config))) 
                       ]
                   (let [
                         key-size (mount-size (:key-size key-config))
                         ;; blank? (:blank key-config)
                         switch-mount (if (:use-key-blocks config)
                                        (switch-mount-block key-size 50)
                                        (switch-mount key-size (:plate-thickness config)))
                         ]
                     (key-place config column row switch-mount)))
        ]
    (apply union switches)))

(defn- caps
  "Keycaps with keycap function"
  [config capfn]
  (let [ config-keys (:keys config) ]
    (apply union
           (for [
                 row (range 0 (count config-keys))
                 column (range 0 (count (nth (:keys config) row)))
                 ]
                   (let [
                         key-size-rotation (:key-size (get-in config-keys [row column]))
                         vert? (not= (.indexOf (name key-size-rotation) "-vert") -1)
                         key-size (if vert? (keyword (clojure.string/replace (name key-size-rotation) #"-vert" "")) key-size-rotation)
                         shape (capfn key-size (:plate-thickness config))
                         ]
             (->> (if vert? (rotate (/ π 2) [0 0 1] shape) shape)
                  (key-place config column row)))))))

(defn switch-holes [config capfn]
  "Switch holes with connectors, optionally pass keycap function"
  (union
    (key-holes config)
    ;; (wire-posts config)
    (when capfn
      (caps config capfn ))
  ))

(defn cut-out 
  "Cut out of plate (to difference with case)"
  [points offset]
  (extrude-linear {:height 500}
                  (polygon (upper-boundary-points points offset))))

(defn cut-out-points
  "Cut out points on a panel"
  [points offset z]
  (mapv #(assoc % 2 z) (mapv first (boundary-points :right points offset)))
  )






;;;;; Testing ;;;;;

;; (spit "things/fun.scad" (write-scad (key-holes temp-config)))

