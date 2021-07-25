(ns mrkabuda.parts.switch_mount
  (:refer-clojure :exclude [use import])
  (:require [clojure.core.matrix :refer [array matrix mmul]]
            [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [mrkabuda.scad.util :refer :all]
            [mrkabuda.parts.keyswitch :refer :all]
            [mrkabuda.keycaps.sa :refer :all]
            [unicode-math.core :refer :all]))

(defn mount-size 
  "The standard is 19mm between 1u switches. This is only used to calculate the mount size.
  If you want a smaller mount size change mount-extra below. config.extra-width/height will
  change the actual spacing where 2mm is standard (just like the mount-extra below (2x1mm)."
  [size-key]
  (let [
        switch-spacing 19
        mount-extra 1 ; each side based on being 0.5 smaller than the standard keycap extra
        calc-full-mount (fn [size] (- (* size switch-spacing) (* mount-extra 2)))
        calc-mount-side (fn [size] (/ (- (calc-full-mount size) keyswitch-width) 2))
        key-dimensions (fn [size]
                         (hash-map  
                                  :width (calc-mount-side size) 
                                  :height (calc-mount-side 1) 
                                  :mount-width (calc-full-mount size) 
                                  :mount-height (calc-full-mount 1)  
                                  ))
        key-dimensions-inverted (fn [size]
                         (hash-map  
                                  :width (calc-mount-side 1) 
                                  :height (calc-mount-side size) 
                                  :mount-width (calc-full-mount 1) 
                                  :mount-height (calc-full-mount size)  
                                  ))
        size-key-str (name size-key)
        vert? (not= (.indexOf size-key-str "-vert") -1)
        key-size (if vert? (clojure.string/replace size-key-str #"-vert" "") size-key-str)
        size (Float. (clojure.string/replace key-size #"u" ""))
        ]
    (if vert?  (key-dimensions-inverted size) (key-dimensions size))))

(def post-size 0.1)
        
(defn plate-post
  [plate-thickness]
  (->> (cube post-size post-size plate-thickness)
       (translate [0 0 (+ (/ plate-thickness -2) plate-thickness)])))

(defn web-post 
  [web-thickness plate-thickness]
  (->> (cube post-size post-size web-thickness)
       (translate [0 0 (+ (/ web-thickness -2) plate-thickness)])))

(defn web-post-corner [corner mount-size web-thickness plate-thickness]
  (let [
        post-adj (/ post-size 2)
        offset-width (- (/ (:mount-width mount-size) 2) post-adj)
        offset-height (- (/ (:mount-height mount-size) 2) post-adj)
        ]
  (translate (case corner
    :tr (map * [1, 1, 1] [offset-width offset-height 0])
    :tl (map * [-1, 1, 1] [offset-width offset-height 0])
    :bl (map * [-1, -1, 1] [offset-width offset-height 0])
    :br (map * [1, -1, 1] [offset-width offset-height 0])
    ) (web-post web-thickness plate-thickness))))

(defn switch-mount [mount-size thickness]
  (let [
        keyswitch-height-adj (/ keyswitch-height 2)
        keyswitch-width-adj (/ keyswitch-width 2)
        top-wall (->> (cube (:mount-width mount-size) (:height mount-size) thickness)
                      (translate [0
                                  (+ (/ (:height mount-size) 2) keyswitch-height-adj)
                                  (/ thickness 2)]))
        left-wall (->> (cube (:width mount-size) (:mount-height mount-size) thickness)
                       (translate [(+ (/ (:width mount-size) 2) keyswitch-width-adj)
                                   0
                                   (/ thickness 2)]))
        side-nub (->> (binding [*fn* 30] (cylinder 1 2.75))
                      (rotate (/ π 2) [1 0 0])
                      (translate [(+ keyswitch-width-adj) 0 1])
                      (hull (->> (cube 1.5 2.75 thickness)
                                 (translate [(+ (/ 1.5 2) keyswitch-width-adj)
                                             0
                                             (/ thickness 2)]))))
        plate-half (union top-wall left-wall (with-fn 100 side-nub))]
    (union plate-half
           (->> plate-half
                (mirror [1 0 0])
                (mirror [0 1 0])))))

(defn switch-mount-block [mount-size thickness]
  (cube (:mount-width mount-size) (:mount-height mount-size) thickness)
  )

(spit "things/fun.scad" (write-scad (switch-mount-block (:1u mount-size) 3)))
