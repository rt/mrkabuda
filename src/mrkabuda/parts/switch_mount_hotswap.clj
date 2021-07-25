(ns mrkabuda.parts.switch_mount_hotswap
  (:refer-clojure :exclude [use import])
  (:require [clojure.core.matrix :refer [array matrix mmul]]
            [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [mrkabuda.scad.util :refer :all]
            [mrkabuda.parts.keyswitch :refer :all]
            [mrkabuda.parts.keycaps.sa :refer :all]
            [unicode-math.core :refer :all]))


;;;;;;;;;;;;;;;;;
;; Switch Hole ;;
;;;;;;;;;;;;;;;;;

(def sa-profile-key-height 12.7)

(def plate-thickness 5)
(def mount-width (+ keyswitch-width 3))
(def mount-height (+ keyswitch-height 3))

(def single-plate
  (let [top-wall (->> (cube (+ keyswitch-width 3) 1.5 plate-thickness)
                      (translate [0
                                  (+ (/ 1.5 2) (/ keyswitch-height 2))
                                  (/ plate-thickness 2)]))
        left-wall (->> (cube 1.5 (+ keyswitch-height 3) plate-thickness)
                       (translate [(+ (/ 1.5 2) (/ keyswitch-width 2))
                                   0
                                   (/ plate-thickness 2)]))
        side-nub (->> (binding [*fn* 30] (cylinder 1 2.75))
                      (rotate (/ π 2) [1 0 0])
                      (translate [(+ (/ keyswitch-width 2)) 0 1])
                      (hull (->> (cube 1.5 2.75 plate-thickness)
                                 (translate [(+ (/ 1.5 2) (/ keyswitch-width 2))
                                             0
                                             (/ plate-thickness 2)]))))
        plate-half (union top-wall
                          left-wall
                          (with-fn 100 side-nub))
        swap-holder (->> (cube (+ keyswitch-width 3) (/ (+ keyswitch-height 3) 2) 3)
                         (translate [0 (/ (+ keyswitch-height 3) 4) -1.5]))
        main-axis-hole (->> (cylinder (/ 4.0 2) 10)
                            (with-fn 12))
        plus-hole (->> (cylinder (/ 2.9 2) 10)
                       (with-fn 8)
                       (translate [-3.81 2.54 0]))
        minus-hole (->> (cylinder (/ 2.9 2) 10)
                        (with-fn 8)
                        (translate [2.54 5.08 0]))
        friction-hole (->> (cylinder (/ 1.7 2) 10)
                           (with-fn 8))
        friction-hole-right (translate [5 0 0] friction-hole)
        friction-hole-left (translate [-5 0 0] friction-hole)
        hotswap-base-shape (->> (cube 14 5.80 1.8)
                                (translate [-1 4 -2.1]))
        hotswap-base-hold-shape (->> (cube (/ 12 2) (- 6.2 4) 1.8)
                                     (translate [(/ 12 4) (/ (- 6.2 4) 1) -2.1]))
        hotswap-pad (cube 4.00 3.0 2)
        hotswap-pad-plus (translate [(- 0 (+ (/ 12.9 2) (/ 2.55 2))) 2.54 -2.1]
                                    hotswap-pad)
        hotswap-pad-minus (translate [(+ (/ 10.9 2) (/ 2.55 2)) 5.08 -2.1]
                                     hotswap-pad)
        wire-track (cube 4 (+ keyswitch-height 3) 1.8)
        column-wire-track (->> wire-track
                               (translate [9.5 0 -2.4]))
        diode-wire-track (->> (cube 2 10 1.8)
                              (translate [-7 8 -2.1]))
        hotswap-base (union
                      (difference hotswap-base-shape
                                  hotswap-base-hold-shape)
                      hotswap-pad-plus
                      hotswap-pad-minus)
        diode-holder (->> (cube 2 4 1.8)
                          (translate [-7 5 -2.1]))
        hotswap-holder (difference swap-holder
                                   main-axis-hole
                                   plus-hole
                                   (mirror [-1 0 0] plus-hole)
                                   minus-hole
                                   (mirror [-1 0 0] minus-hole)
                                   friction-hole-left
                                   friction-hole-right
                                   hotswap-base
                                   (mirror [-1 0 0] hotswap-base))]
    (difference (union plate-half
                       (->> plate-half
                            (mirror [1 0 0])
                            (mirror [0 1 0]))
                       hotswap-holder)
                #_diode-holder
                #_diode-wire-track
                column-wire-track)))

(spit "things/fun.scad" (write-scad single-plate))
