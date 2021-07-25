(ns mrkabuda.scad.util
  (:refer-clojure :exclude [use import])
  (:require [clojure.core.matrix :refer [array matrix mmul]]
            [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]))

(defn deg2rad [degrees]
  (* (/ degrees 180) pi))

(defn rotate-around-x [angle pos]
  (mmul
    [[1 0 0]
     [0 (Math/cos angle) (- (Math/sin angle))]
     [0 (Math/sin angle)    (Math/cos angle)]]
    pos))

(defn rotate-around-y [angle pos]
  (mmul
    [[(Math/cos angle)     0 (Math/sin angle)]
     [0                    1 0]
     [(- (Math/sin angle)) 0 (Math/cos angle)]]
    pos))

(defn rotate-around-z [angle pos]
  (mmul
    [[(Math/cos angle)     (- (Math/sin angle)) 0]
     [(Math/sin angle) (Math/cos angle) 0]
     [0                    0 1]]
    pos))

;; (defn frags [radius]
;;     (if (= *fn* 0) 
;;       (max (min (/360 *fa*) (/ (* radius Math/PI 2) *fs*)) 5)
;;       (if (>= *fn* 3) *fn* 3)

(defn nearest_multiple_of_4 [n]
    (let [remain (mod n 4)]
    (if (> (/ remain 4) 0.5) 
      (+ (- n remain) 4) 
      (- n remain))))

(defn triangle-hulls [& shapes]
  (apply union
         (map (partial apply hull)
              (partition 3 1 shapes))))
