(ns mrkabuda.keyboards.ergo_thumb
  (:refer-clojure :exclude [use import])
  (:require 
            [clojure.core.matrix :refer [array matrix mmul]]
            [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [mrkabuda.scad.util :refer :all]
            [mrkabuda.keycaps.sa :refer :all]
            [mrkabuda.parts.switch_mount :refer :all]
            [mrkabuda.parts.plate :refer :all]
            [mrkabuda.parts.plate_utils :refer :all]
            [mrkabuda.cases.wall_brace :refer :all]
            [mrkabuda.parts.key_hole_connectors :refer :all]
            [mrkabuda.parts.screw_insert :refer :all]
            [unicode-math.core :refer :all]))
  ;; (:gen-class))

(defn keyboard 
  [config]
  "Finger plate, Thumb plate, Merge them, Tent/Pronate
  Translate, Mirror, Concat, Prep walls, Walls"
  (let [
        plate-config (:plate config)
        thumb-config (:thumb-config config)
        tenting-angle (:tenting-angle config)
        pronation-angle (:pronation-angle config)
        thumb-origin (map + 
                          (key-position plate-config (:homecol plate-config) (:homerow plate-config) [0 0 0]) 
                          (map - (key-position thumb-config (:homecol thumb-config) (:homerow thumb-config) [0 0 0])) 
                          (:thumb-origin config))
        ;; physical right side
        model-right (->> (union
                           (switch-holes (:plate config) nil)
                           (translate thumb-origin (switch-holes (:thumb-config config) nil))
                           )
                         (rotate tenting-angle [0 1 0])
                         (rotate pronation-angle [0 0 1]))
        ;; data points to match physical
        plate-points (tent-and-pronate-plate tenting-angle pronation-angle (switch-hole-positions plate-config (/ post-size 2)))
        thumb-plate-points (tent-and-pronate-plate 
                             tenting-angle 
                             pronation-angle 
                             (translate-plate thumb-origin (switch-hole-positions (:thumb-config config) (/ post-size 2))))

        translation [(+ (- (min-x plate-points)) (:split-length config)) (- (/ (plate-height plate-points) 2) (max-y plate-points)) (:keyboard-z-offset config)]

        ;; physical translated so that it can be mirrored
        keeb-half (translate translation model-right)

        ;; data point to match physical
        translated-plate-points (translate-plate translation plate-points)
        translated-thumb-plate-points (translate-plate translation thumb-plate-points)

        ;; data points to match main below
        all-plate-points (map concat (map mirror-plate translated-plate-points) translated-plate-points)
        all-thumb-plate-points (map concat (map mirror-plate translated-thumb-plate-points) translated-thumb-plate-points)
        ]
    ;; physical
    (let [plate-thickness (:plate-thickness config)]
    (difference
      (union
        keeb-half
        (mirror [1 0 0] keeb-half)

        (connectors all-plate-points)
        (connectors all-thumb-plate-points)

        (wall-back plate-thickness all-plate-points)
        (wall-corner :tr plate-thickness all-plate-points)
        (wall-right plate-thickness all-plate-points)
        (wall-corner :br plate-thickness all-plate-points)
        ;; (wall-front plate-thickness all-plate-points)
        (wall-corner :bl plate-thickness all-plate-points)
        (wall-left plate-thickness all-plate-points)
        (wall-corner :tl plate-thickness all-plate-points)

        ;; (wall-back plate-thickness all-thumb-plate-points)
        ;; (wall-corner :tr plate-thickness all-thumb-plate-points)
        (wall-right plate-thickness all-thumb-plate-points)
        (wall-corner :br plate-thickness all-thumb-plate-points)
        (wall-front plate-thickness all-thumb-plate-points)
        (wall-corner :bl plate-thickness all-thumb-plate-points)
        (wall-left plate-thickness all-thumb-plate-points)
        ;; (wall-corner :tl plate-thickness all-thumb-plate-points)

        )
      (translate [0 0 -20] (cube 350 350 40))
      )))

  ;; (cut
  ;;   (translate [0 0 -0.1]
  ;;              (difference (union (case-walls config (switch-hole-positions config (/ post-size 2)) )
  ;;                                 screw-insert-outers)
  ;;                          (translate [0 0 -10] screw-insert-screw-holes))
  ;;              ))
  )

