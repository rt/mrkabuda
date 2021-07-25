(ns mrkabuda.core
  (:refer-clojure :exclude [use import])
  (:require 
            [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [mrkabuda.utils.utils :refer :all]
            [mrkabuda.parts.plate :refer :all]
            [mrkabuda.config_data.config_generator :refer :all]
            [mrkabuda.keyboards.ergo_thumb :as ergo-thumb]
            [mrkabuda.keyboards.standard :as standard]
            [unicode-math.core :refer :all]
            ))
  ;; (:gen-class))
(def plate-thickness 4)
(def web-thickness 3.5)

(defn create-config [overrides]
  (let [
        plate-thickness 4
        web-thickness 3.5
        ]
    (deep-merge {
                 :plate-thickness plate-thickness
                 :thumb-origin [-45 -40 4]
                 :keyboard-z-offset 7           ;; controls overall height; original=9 with centercol=3; use 16 for centercol=2
                 :tenting-angle 0
                 :pronation-angle 0
                 :split-length 0

                 :plate {
                              :homerow 1
                              :homecol 1
                              :plate-thickness plate-thickness
                              :web-thickness web-thickness
                              :centercol 4                   ;; controls left-right tilt / tenting (higher number is more tenting)
                              :centerrow 1                   ;; controls front-back tilt
                              :col-curvature (/ π 12)        ;; curvature of the columns
                              :row-curvature (/ π 36)        ;; curvature of the rows
                              :column-style :standard        ;; :gmk ...
                              :sa-profile-key-height 12.7
                              :extra-width 2.5               ;; extra space between the base of keys; original= 2
                              :extra-height 1.0              ;; original= 0.5
                              }

                 :thumb-config {
                                :homerow 0
                                :homecol 1
                                :plate-thickness plate-thickness
                                :web-thickness web-thickness
                                :centercol 4                   ;; controls left-right tilt / tenting (higher number is more tenting)
                                :centerrow 1                   ;; controls front-back tilt
                                :keys []
                                :extra-width 2.5               ;; extra space between the base of keys; original= 2
                                :extra-height 1.0              ;; original= 0.5
                                }
                 } overrides)))

(defn plate-standard []
  {
   :plate-type :standard ; :ortho
   :mirrored false
   :split-length 15
   :pos :center ; :tl :tr :bl ;br
   :offset [0 0 0] ; this would replace keyboard-z-offset, right?
   :keyboard-z-offset 15 ;; if standard then it should be the same ase panel-height
   :homerow 1
   :homecol 1
   :plate-thickness plate-thickness
   :web-thickness web-thickness
   :centercol 4                   ;; controls left-right tilt / tenting (higher number is more tenting)
   :centerrow 1                   ;; controls front-back tilt
   :col-curvature (/ π 12)        ;; curvature of the columns
   :row-curvature (/ π 36)        ;; curvature of the rows
   :column-style :standard        ;; :gmk ...
   :sa-profile-key-height 12.7
   :extra-width 2.0
   :extra-height 2.0
   :tenting-angle 0
   :pronation-angle 0
   :keys keys-40 
   ;; :keys keys-60 
   ;; :keys keys-boardwalk
   ;; :keys keys-boardwalk-arrow-cluster

   ;; ortho layouts
   ;; :keys keys-minidox
   ;; :keys keys-tenkey
   ;; :keys keys-planck 
   ;; :keys keys-boardwalk-ergodox
   }
  )

(defn plate-thumb []
  {
   :plate-type :standard ;:ergo
   :mirrored true
   :split-length 10
   :pos :center ; :br :tr :bl :tl
   :offset [0 -45 0] ; this would replace keyboard-z-offset, right?
   :keyboard-z-offset 35 ;; if standard then it should be the same ase panel-height
   :homerow 1
   :homecol 1
   :plate-thickness plate-thickness
   :web-thickness web-thickness
   :extra-width 2.0
   :extra-height 2.0
   :centercol 2                   ;; controls left-right tilt / tenting (higher number is more tenting)
   :centerrow 0                   ;; controls front-back tilt
   :keys (vector
           (vector 
             {
              :angle-offset {:x 10 :y -23 :z 10}
              ;; :angle-offset {:x 0 :y 0 :z 0}
              :offset [0 -14 -12] 
              ;; :offset [0 0 0]
              :key-size :1u 
              }
             {
              :angle-offset {:x 10 :y -23 :z 10}
              ;; :angle-offset {:x 0 :y 0 :z 0}
              :offset [0 -15 -2] 
              ;; :offset [0 0 0]
              :key-size :1u 
              }
             {
              :angle-offset {:x 10 :y -23 :z 10}
              ;; :angle-offset {:x 0 :y 0 :z 0}
              :offset [0 -16 3] 
              ;; :offset [0 0 0]
              :key-size :1u 
              }
             ))
   :col-curvature (/ π 12)        ;; curvature of the columns
   :row-curvature (/ π 36)        ;; curvature of the rows
   :column-style :standard        ;; :gmk ...
   :sa-profile-key-height 12.7
   :tenting-angle (/ π 16)
   :pronation-angle (/ π 8)
   }
  )

(defn plate-ergo []
  {
   :plate-type :ergo ; :ortho :standard
   :pos :center ; :br :tr :bl :tl
   :offset [0 0 0] ; this would replace keyboard-z-offset, right?
   :keyboard-z-offset 15 ;; if standard then it should be the same ase panel-height
   :mirrored true
   :split-length 10
   :tenting-angle (/ π 16)
   :pronation-angle (/ π 8)
   :homerow 1
   :homecol 1
   :plate-thickness plate-thickness
   :web-thickness web-thickness
   :centercol 4                   ;; controls left-right tilt / tenting (higher number is more tenting)
   :centerrow 2                   ;; controls front-back tilt
   :col-curvature (/ π 12)        ;; curvature of the columns
   :row-curvature (/ π 36)        ;; curvature of the rows
   :column-style :standard        ;; :gmk ...
   :sa-profile-key-height 12.7
   :extra-width 2
   :extra-height 2

   :keys (config-keys 3 5 
                      (fn [row column] 
                        (cond
                          (= column 2) 
                          {
                           :key-size :1u 
                           :angle-offset {:x 0 :y 0 :z 0}
                           :offset [0 2.82 -4.5]
                           }
                          (>= column 4) 
                          {
                           :key-size :1u 
                           :angle-offset {:x 0 :y 0 :z 0}
                           :offset [0 -12 5.64]            ; original [0 -5.8 5.64]
                           }
                          :else 
                          {
                           :key-size :1u 
                           :angle-offset {:x 0 :y 0 :z 0}
                           :offset [0 0 0]
                           })))

   ;; :keys (config-keys 4 6 
   ;;                    (fn [row column] 
   ;;                      (cond
   ;;                        :else 
   ;;                        {
   ;;                         :key-size :1u 
   ;;                         :angle-offset {:x 0 :y 0 :z 0}
   ;;                         :offset [0 0 0]
   ;;                         })))
   }
  )

(def standard-config 
  "Uses panels, mirrors halves, multiple plates" 
  (let [
        plate-thickness 4
        web-thickness 3.5
        ]
    (create-config {
                    :type :standard
                    :panel-tilt-angle 10
                    :plate-panel-offset 8
                    :radius 3
                    :panel-height 25
                    :top-panel-length 350
                    :top-panel-width 150
                    :plates [
                             (plate-standard)
                             ;; (plate-ergo)
                             ;; (plate-thumb)
                             ]
                    })))

(def ergo-thumb-config 
  "Uses wall braces, leaning towards deprecating this"
  (create-config {
                  :type :ergo-thumb
                  :thumb-origin [-25 -50 4]
                  :tenting-angle (/ π 16)
                  :pronation-angle (/ π 8)
                  :split-length 30
                  :plate {
                          :plate-type :ergo
                          :keys (config-keys 3 5 
                                             (fn [row column] 
                                               (cond
                                                 (= column 2) 
                                                 {
                                                  :key-size :1u 
                                                  :angle-offset {:x 0 :y 0 :z 0}
                                                  :offset [0 2.82 -4.5]
                                                  }
                                                 (>= column 4) 
                                                 {
                                                  :key-size :1u 
                                                  :angle-offset {:x 0 :y 0 :z 0}
                                                  :offset [0 -12 5.64]            ; original [0 -5.8 5.64]
                                                  }
                                                 :else 
                                                 {
                                                  :key-size :1u 
                                                  :angle-offset {:x 0 :y 0 :z 0}
                                                  :offset [0 0 0]
                                                  })))

                          }

                  :thumb-config {
                                 :plate-type :standard
                                 :centercol 2                   ;; controls left-right tilt / tenting (higher number is more tenting)
                                 :centerrow 0                   ;; controls front-back tilt
                                 :keys (vector
                                         (vector 
                                           {
                                            :angle-offset {:x 10 :y -23 :z 10}
                                            :offset [0 -14 -12] 
                                            :key-size :1u 
                                            }
                                           {
                                            :angle-offset {:x 10 :y -23 :z 10}
                                            :offset [0 -15 -2] 
                                            :key-size :1u 
                                            }
                                           {
                                            :angle-offset {:x 10 :y -23 :z 10}
                                            :offset [0 -16 3] 
                                            :key-size :1u 
                                            }
                                           ))
                                 }
                  }))

(defn get-keyboard [config]
  "main entry point for creating a keyboard"
  (case (:type config)
    :ergo-thumb (ergo-thumb/keyboard config)
    :standard (standard/keyboard config)
    ))

(spit "things/keyboard.scad" (write-scad (get-keyboard 
                                        standard-config
                                        ;; ergo-thumb-config
                                        )))

(defn -main [dum] 1)  ; dummy to make it easier to batch

