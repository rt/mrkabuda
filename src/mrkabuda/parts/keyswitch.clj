(ns mrkabuda.parts.keyswitch
  (:refer-clojure :exclude [use import])
  (:require [clojure.core.matrix :refer [array matrix mmul]]
            [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [mrkabuda.scad.util :refer :all]
            [unicode-math.core :refer :all]))


(def keyswitch-height 14.4) ;; Was 14.1, then 14.25
(def keyswitch-width 14.4)
