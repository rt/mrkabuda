(ns mrkabuda.config_data.config_generator
  (:refer-clojure :exclude [use import])
  (:require 
            [mrkabuda.utils.utils :refer :all]
            [mrkabuda.scad.util :refer :all]
            [unicode-math.core :refer :all]))
  ;; (:gen-class))

(defn config-keys 
  "Quick way to get keys of uniformity."
  [rows cols change-fn]
  (vec
    (for [ row (range 0 rows) ]
      (vec
        (for [ col (range 0 cols) ]
          (change-fn row col)
      )))))

(defn- key-config-details
  [options]
  (let [
        ]
    {
     :zero-width (or (:zero-width options) false)
     :zero-height (or (:zero-height options) false)
     :key-size (or (:key-size options) :1u)
     :angle-offset (or (:angle-offset options) {:x 0 :y 0 :z 0})
     :offset (or (:offset options) [0 0 0])
     })
  )

(defn- key-config [size] 
  (let [
        zero-width? (not= (.indexOf (name size) "-0w") -1)
        zero-height? (not= (.indexOf (name size) "-0h") -1)
        key-size (keyword (clojure.string/replace (name size) #"(-0w|-0h)" ""))
        ]
    (key-config-details {
                         :zero-width zero-width?
                         :zero-height zero-height?
                         :key-size key-size 
                         })))

(defn- key-configs [configs]
  (map key-config configs))

(def numbers-left
  (vec
    (for [key (range 1 6)]
      (key-config :1u)
      )
    ))

(def numbers-right
  (vec
    (for [key (range 6 11)]
      (key-config :1u)
      )
    ))

(def numbers (concat numbers-left numbers-right))

(defn char-range [start end]
  (map char (range (int start) (inc (int end)))))

(def qwerty-1-left [ \q \w \e \r \t ])
(def qwerty-1-right [ \y \u \i \o \p ])
(def qwerty-1 (concat qwerty-1-left qwerty-1-right))
(def qwerty-2-left [ \a \s \d \f \g ]) 
(def qwerty-2-right [ \h \j \k \l ])
(def qwerty-2 (concat qwerty-2-left qwerty-2-right))
(def qwerty-3-left [ \z \x \c \v \b ])
(def qwerty-3-right [ \n \m ])
(def qwerty-3 (concat qwerty-3-left qwerty-3-right))

(defn alphas [alpha-chars]
  (vec
    (map (fn [ch] (key-config :1u)) alpha-chars)
    ))

(def keys-minidox
  "ortho 4x5u"
  (vector
   (vec (concat  (key-configs [:1u :1u :1u :1u :1u])))
   (vec (concat  (key-configs [:1u :1u :1u :1u :1u])))
   (vec (concat  (key-configs [:1u :1u :1u :1u :1u])))
   (vec (concat (vector 
                  (key-config-details {
                                       :angle-offset {:x 0 :y 0 :z 10}
                                       :offset [-10 -15 -10]
                                       })
                  (key-config-details {
                                       :angle-offset {:x 0 :y 0 :z 10}
                                       :offset [-10 -10 0]
                                       })
                  (key-config-details {
                                       :angle-offset {:x 0 :y 0 :z 10}
                                       :offset [-10 -5 0]
                                       })
                  ) 
                (key-configs [:1u-0w :1u-0w])))
   ))

(def keys-planck
  "ortho 4x12u"
  (vector
   (vec (concat (key-configs [:1u]) (alphas qwerty-1) (key-configs [:1u])))
   (vec (concat (key-configs [:1u]) (alphas qwerty-2) (key-configs [:1u :1u])))
   (vec (concat (key-configs [:1u]) (alphas qwerty-3) (key-configs [:1u :1u :1u :1u])))
   ;; (vec (concat (key-configs [:1u :1u :1u :1u :1u :2u :1u-0w :1u :1u :1u :1u :1u])))
   (vec (concat (key-configs [:1u :1u :1u :1u :2u :1u-0w :2u :1u-0w :1u :1u :1u :1u])))
   ))

(def keys-boardwalk
  "staggard 5x15u"
  (vector
   (vec (concat (key-configs [:1.5u]) numbers-left (key-configs [:1u :1u ]) numbers-right (key-configs [:1.5u])))
   (vec (concat (key-configs [:1.5u]) (alphas qwerty-1-left) (key-configs [:1u :1u ]) (alphas qwerty-1-right) (key-configs [:1.5u])))
   (vec (concat (key-configs [:1.5u]) (alphas qwerty-2-left) (key-configs [:1u :1u ]) (alphas qwerty-2-right) (key-configs [:1u :1.5u])))
   (vec (concat (key-configs [:1.5u]) (alphas qwerty-3-left) (key-configs [:1u :1u ]) (alphas qwerty-3-right) (key-configs [:1u :1u :1u :1.5u])))
   ;; (vec (concat (key-configs [:1.5u-0h :1u :1u :1u :1u :2u :2u :1u :1u :1u :1u :1.5u-0h])))
   (vec (concat (key-configs [:1.5u-0h :1u :1.5u :7u :1.5u :1u :1.5u-0h])))
   ))

(def keys-boardwalk-ergodox
  "staggard 5x15u"
  (vector
   (vec (concat (key-configs [:1.5u]) numbers-left (key-configs [:1u :1u ]) numbers-right (key-configs [:1.5u])))
   (vec (concat (key-configs [:1.5u]) (alphas qwerty-1-left) (key-configs [:1.5u-vert :1.5u-vert ]) (alphas qwerty-1-right) (key-configs [:1.5u])))
   (vec (concat (key-configs [:1.5u]) (alphas qwerty-2-left) (key-configs [:1.5u-vert :1.5u-vert ]) (alphas qwerty-2-right) (key-configs [:1u :1.5u])))
   (vec (concat (key-configs [:1.5u]) (alphas qwerty-3-left) (key-configs [:1u-0h :1u-0h ]) (alphas qwerty-3-right) (key-configs [:1u :1u :1u :1.5u])))
   (vec (concat (key-configs [:1.5u-0h :1u :1u :1u :1u :2u :1u-0w :2u :1u-0w :1u :1u :1u :1u :1.5u-0h])))
   ))

(def keys-boardwalk-arrow-cluster
  "staggard 5x15u"
  (vector
   (vec (concat (key-configs [:1.5u]) numbers (key-configs [:1u :1u :1.5u])))
   (vec (concat (key-configs [:1.5u]) (alphas qwerty-1) (key-configs [:1u :1u :1.5u])))
   (vec (concat (key-configs [:1.5u]) (alphas qwerty-2) (key-configs [:1u :1u :1.5u :1u])))
   (vec (concat (key-configs [:1.5u]) (alphas qwerty-3) (key-configs [:1u :1u :1u :1.5u :1u :1u])))
   (vec (concat (key-configs [:1.25u :1.25u :1u :1u :2u :2u :1u :1.25u :1.25u :1u :1u :1u])))
   ))


(def keys-tenkey
  "staggard 4x12u"
  (vector
   (vec (concat (key-configs [:1u :1u :1u])))
   (vec (concat (key-configs [:1u :1u :1u])))
   (vec (concat (key-configs [:1u :1u :1u])))
   (vec (concat (key-configs [:2u :0u :1u])))
   ))

(def keys-40
  "staggard 4x12u"
  (vector
   (vec (concat (key-configs [:1u]) (alphas qwerty-1) (key-configs [:1u])))
   (vec (concat (key-configs [:1.25u]) (alphas qwerty-2) (key-configs [:1.75u])))
   (vec (concat (key-configs [:1.75u]) (alphas qwerty-3) (key-configs [:1u :1u :1.25u ])))
   ;; (vec (concat (key-configs [:1.25u :1.25u :1.25u :2.25u :2.25u :1.25u :1u :1.5u])))
   (vec (concat (key-configs [:1.5u-0h :1.25u :1.25u :1.25u :2.25u :1u :1u :1u :1.5u-0h])))
   ))

(def keys-60
  "staggard 5x15u"
  (vector
   (vec (concat (key-configs [:1u]) numbers (key-configs [:1u :1u :2u])))
   (vec (concat (key-configs [:1.5u]) (alphas qwerty-1) (key-configs [:1u :1u :1.5u])))
   (vec (concat (key-configs [:1.75u]) (alphas qwerty-2) (key-configs [:1u :1u :2.25u])))
   (vec (concat (key-configs [:2.25u]) (alphas qwerty-3) (key-configs [:1u :1u :1u :2.75u])))
   ;; (vec (concat (key-configs [:1.25 :1.25 :1.25 :6.25 :1.25 :1.25 :1.25 :1.25])))
   (vec (concat (key-configs [:1.5u-0h :1u :1.5u :6u :1.5u :1u :2.5u-0h])))
   ;; (vec (concat (key-configs (vec (repeat 15 :1u)))))
   ))


(def key-configs-36 [
                     [
                          {
                           :logical-ref {
                                         :char "a"
                                         :qmk "LT(KC_SPACE)"
                                         :finger 1 ;layout-analyzer
                           }
                           :key-size :2u
                           :angle-offset {:x 10 :y -23 :z 10}
                           :offset [-52 -14 -12] 
                           }
                      ]
                     []
                     []
                     []
                     ])
