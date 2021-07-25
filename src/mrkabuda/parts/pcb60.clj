(ns mrkabuda.parts.pcb60
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]))


(def pcb60-length 285.000)
(def pcb60-length-half (/ pcb60-length 2))
(def pcb60-width 94.600)
(def pcb60-width-half (/ pcb60-width 2))
(def pcb60-screw {
                  :type "M2"
                  :length "4"
                  :radius 1.000 ; guestimate
                  :diameter 2; guestimate
                  })

(def pcb60-usb {
                :offset-x 18.200
                :offset-y 0
                :offset-z 5
                :width 10 ; gh60 case
                :height 6.500 ; gh60 case
                })

(def pcb60-reset-button {
                   :offset-x 29.150
                   :offset-y -48.200
                   :width 12 ; gh60 case
                   :height 12.500 ; gh60 case
                   })

;; offsets are from top-left
(def pcb60-holes [
                  {
                   :name "topleft" 
                   :offset-x 25.200
                   :offset-y -27.900
                   }
                  {
                   :name "topright" 
                   :offset-x (- pcb60-length 24.950)
                   :offset-y -27.900
                   }
                  {
                   :name "sideleft" 
                   :offset-x 3.500 ;;guestimate
                   :offset-y -56.500
                   }
                  {
                   :name "sideright" 
                   :offset-x (- pcb60-length 3.500) ; guestimate
                   :offset-y -56.500
                   }
                  {
                   :name "middleleft" 
                   :offset-x 128.200 ;; from left side
                   :offset-y -47.000
                   }
                  {
                   :name "middleright" 
                   :offset-x 190.500 ;; from left side
                   :offset-y -85.200
                   }
                  ])

