(ns mrkabuda.keyboards.standard
  (:refer-clojure :exclude [use import])
  (:require 
            [clojure.core.matrix :refer [array matrix mmul]]
            [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [mrkabuda.utils.utils :refer :all]
            [mrkabuda.scad.util :refer :all]
            [mrkabuda.parts.switch_mount :refer :all]
            [mrkabuda.cases.panel :refer :all]
            [mrkabuda.parts.key_hole_connectors :refer :all]
            [mrkabuda.parts.plate :refer :all]
            [mrkabuda.parts.plate_utils :refer :all]
            [mrkabuda.cases.case60 :refer :all]
            [unicode-math.core :refer :all]))
  ;; (:gen-class))

(defn panel-to-plate
  [config plate-config plate-points]
  (let [
        panel-height (:panel-height config)
        plate-panel-offset (:plate-panel-offset config)
        plate-thickness (:plate-thickness plate-config)
        ]
  (union
      (map #(connect-all (map-boundary-to-level plate-points % panel-height plate-panel-offset plate-thickness)) [:right :left :top :bottom])

      (connect-pairs
             (conj (boundary-corner-points :tl plate-points panel-height plate-panel-offset plate-thickness) (vec (first (first plate-points)))))
      (connect-pairs
             (conj (boundary-corner-points :tr plate-points panel-height plate-panel-offset plate-thickness) (vec (last (first plate-points)))))
      (connect-pairs
             (conj (boundary-corner-points :bl plate-points panel-height plate-panel-offset plate-thickness) (vec (first (last plate-points)))))
      (connect-pairs
             (conj (boundary-corner-points :br plate-points panel-height plate-panel-offset plate-thickness) (vec (last (last plate-points)))))
    )))

(defn panel-offset-pos
  "Get position of offset to tr, tl, br, bl, center"
  [length width points-width points-height pos offset]
  (let [
        multiplier (case pos 
                :tl [-1 1 0]
                :tr [1 1 0]
                :bl [-1 -1 0]
                :br [1 -1 0]
                :center [0 0 0]
                )
        points-offset (map * [-1 -1 -1] multiplier [(/ points-width 2) (/ points-height 2) 0])
        ]
  (map + points-offset offset (map * [(/ length 2) (/ width 2) 0] multiplier))))
  
(defn standard-plate-points
  ""
  [config plate-config]
  (let [
        ;; center and do what is needed while in level stage
        mirror-translation-standard (fn [config plate-config plate-points]
                             [ (:split-length plate-config)
                              (- (/ (plate-height plate-points) 2) (max-y plate-points))
                              0]
                             )
        center-translation-standard (fn [config plate-config plate-points] (mapv * [-1 1 0] (center-point plate-points)))
        
        mirror-translation-ergo (fn [config plate-config plate-points]
                             [(+ (- (min-x plate-points)) (:split-length plate-config))
                              (- (/ (plate-height plate-points) 2) (max-y plate-points))
                              0]
                             )

        center-translation-ergo (fn [config plate-config plate-points]
                             (mapv * [-1 -1 0] (center-point-opposite-polarity plate-points))
                             )
        mirror-translation (case (:plate-type plate-config)
              :ergo mirror-translation-ergo 
              mirror-translation-standard)

        center-translation (case (:plate-type plate-config)
              :ergo center-translation-ergo 
              center-translation-standard)

        raw-positions (switch-hole-positions plate-config (/ post-size 2))
        translation (if (:mirrored plate-config) 
                      (mirror-translation config plate-config raw-positions) 
                      (center-translation config plate-config raw-positions))
        translated-plate-points (translate-plate translation raw-positions)
        plate-points (tent-and-pronate-plate 
                       (:tenting-angle plate-config) 
                       (:pronation-angle plate-config) 
                       translated-plate-points)

        ;; points
        mirrored-points (map concat (map mirror-plate plate-points) plate-points)
        points (if (:mirrored plate-config) mirrored-points plate-points)

        tilted-points (tilt-plate (deg2rad (:panel-tilt-angle config)) points)
        
        position (panel-offset-pos (:top-panel-length config) (:top-panel-width config) (plate-width points) (plate-height points) (:pos plate-config) (:offset plate-config))
        absolute-points (translate-plate position points)
        absolute-tilted-points (translate-plate position tilted-points)

        heightened-translation [0 0 (:keyboard-z-offset plate-config)]
        heightened-points (translate-plate heightened-translation absolute-points)
        tilted-and-heightened-points (translate-plate heightened-translation absolute-tilted-points)
        ]
    {
     :raw-positions raw-positions 
     :translated-plate-points translated-plate-points
     :points heightened-points ;;cutouts use non-tilted points
     :tilted-points tilted-and-heightened-points
     :translation translation
     :heightened-translation heightened-translation
     :position position
     }))

(defn- cutout
  [config plate-config plate-points]
  (let [
        centered-blocks (translate (:translation plate-points) (switch-holes (deep-merge plate-config {:use-key-blocks true}) nil))
        shape (if (:mirrored plate-config) (union centered-blocks (mirror [1 0 0] centered-blocks)) centered-blocks)
        ]
    ;; (translate (:position plate-points) shape) ;; this was the cutout for the unibody (might still be a pattern))
    (cut-out (:points plate-points) (:plate-panel-offset config))
    ))

(defn physical-plate
  [config plate-config plate-points]
  (let [
        shape (->> (union
                     (translate (:translation plate-points) (switch-holes plate-config nil))
                     (connectors (:translated-plate-points plate-points))
                     )
                   (rotate (:tenting-angle plate-config) [0 1 0])
                   (rotate (:pronation-angle plate-config) [0 0 1])
                   )
        heightened-mounts (translate (:heightened-translation plate-points) shape)
        mirrored-shape (union heightened-mounts (mirror [1 0 0] heightened-mounts))
        absolute-shape (translate (:position plate-points) (if (:mirrored plate-config) mirrored-shape heightened-mounts))
        ]
    ;; translate to where the panel z=0, rotate and translate back
    (translate [0 0 (:panel-height config)]
               (rotate (deg2rad (:panel-tilt-angle config)) [1 0 0] 
                       (translate [0 0 (- (:panel-height config))]
                                  (union
                                    absolute-shape
                                    (panel-to-plate config plate-config (:points plate-points))
                                    ))))
  ))

(defn top-panel-points
  [config]
  (let [
        top-panel-points (panel-positions (:top-panel-length config) (:top-panel-width config) (:radius config)) ; panels are already centered
        tilted-panel-points (tilt-panel-points (deg2rad (:panel-tilt-angle config)) top-panel-points)
        height-panel-points (translate-panel [0 0 (:panel-height config)] tilted-panel-points)
        ]
    {
     :outer-points height-panel-points
     } 
    ))

(defn keyboard
  [config] 
  (let [
        plates (:plates config)
        ]
    (difference
      (union
        ;; for plates
        (for [plate-config plates]
          (physical-plate config plate-config 
           (standard-plate-points config plate-config))
          )

        ;; top-panel
        (translate [0 0 (:panel-height config)]
                   (rotate (deg2rad (:panel-tilt-angle config)) [1 0 0]
                           (difference
                             (plate-holder (:top-panel-length config) (:top-panel-width config) (:radius config))
                             (for [plate-config plates]
                               (cutout config plate-config
                                       (standard-plate-points
                                        config plate-config))
                               )))
                   )

        ;; walls
        (make-walls (:radius config) (:outer-points (top-panel-points config)))

        ;; (color [0 255 0 255] case60)
        )
      ;; (translate [0 0 -40] (cube 800 800 80))
      )))


