(ns mrkabuda.cases.case60
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [mrkabuda.parts.pcb60 :refer :all]))


(def case60-thickness 3.5)
(def case60-length (+ pcb60-length (* case60-thickness 2)))
(def case60-width (+ pcb60-width (* case60-thickness 2)))
(def case60-height 14.4)
(def case60-support-height 5.000) ; the internal height of the pcb supports

;; at the height they will exist in the case
(defn screw-support [offset-x offset-y]
  (translate [offset-x offset-y (+ case60-thickness (/ case60-support-height 2))]
  (cylinder 3 case60-support-height)))

(defn translate-pcb-offset [model] (translate [(- 0 pcb60-length-half) pcb60-width-half 0] model))

(def screw-supports
  (translate-pcb-offset 
             (union
               (for [hole pcb60-holes] 
                 (screw-support (hole :offset-x) (hole :offset-y)))))
  )

(def reset-plug
  (->> (cube (pcb60-reset-button :width) (pcb60-reset-button :height) case60-thickness)
       (translate [(pcb60-reset-button :offset-x) (pcb60-reset-button :offset-y) (/ case60-thickness 2)])
       (translate-pcb-offset)
  ))

(def usb-plug
  (->> (cube (pcb60-usb :width) case60-thickness (pcb60-usb :height))
       (translate [(pcb60-usb :offset-x) (/ case60-thickness 2) (pcb60-usb :offset-z)])
       (translate-pcb-offset)
       )
  )

;; shell translated to z = 0
(def shell
  (translate [0 0 (/ case60-height 2)] 
             (difference
               (cube case60-length case60-width case60-height)
               (translate [0 0 case60-thickness] 
                          (cube (- case60-length case60-thickness) (- case60-width case60-thickness) case60-height)
                          ))))

(def case60
  (union
  (difference shell usb-plug reset-plug)
  screw-supports
  ))


(spit "things/fun.scad" (write-scad case60))

