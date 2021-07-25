
;;;;;;;;;;;;;;;;
;; Teensy Holder ;;
;;;;;;;;;;;;;;;;

;; (def teensy-width 20)
;; (def teensy-height 12)
;; (def teensy-length 33)
;; (def teensy2-length 53)
;; (def teensy-pcb-thickness 2)
;; (def teensy-holder-width  (+ 7 teensy-pcb-thickness))
;; (def teensy-holder-height (+ 6 teensy-width))
;; (def teensy-offset-height 5)
;; (def teensy-holder-top-length 18)
;; (def teensy-top-xy (key-position 0 (- centerrow 1) (wall-locate3 -1 0)))
;; (def teensy-bot-xy (key-position 0 (+ centerrow 1) (wall-locate3 -1 0)))
;; (def teensy-holder-length (- (second teensy-top-xy) (second teensy-bot-xy)))
;; (def teensy-holder-offset (/ teensy-holder-length -2))
;; (def teensy-holder-top-offset (- (/ teensy-holder-top-length 2) teensy-holder-length))
;;
;; (def teensy-holder
;;     (->>
;;         (union
;;           (->> (cube 3 teensy-holder-length (+ 6 teensy-width))
;;                (translate [1.5 teensy-holder-offset 0]))
;;           (->> (cube teensy-pcb-thickness teensy-holder-length 3)
;;                (translate [(+ (/ teensy-pcb-thickness 2) 3) teensy-holder-offset (- -1.5 (/ teensy-width 2))]))
;;           (->> (cube 4 teensy-holder-length 4)
;;                (translate [(+ teensy-pcb-thickness 5) teensy-holder-offset (-  -1 (/ teensy-width 2))]))
;;           (->> (cube teensy-pcb-thickness teensy-holder-top-length 3)
;;                (translate [(+ (/ teensy-pcb-thickness 2) 3) teensy-holder-top-offset (+ 1.5 (/ teensy-width 2))]))
;;           (->> (cube 4 teensy-holder-top-length 4)
;;                (translate [(+ teensy-pcb-thickness 5) teensy-holder-top-offset (+ 1 (/ teensy-width 2))])))
;;         (translate [(- teensy-holder-width) 0 0])
;;         (translate [-1.4 0 0])
;;         (translate [(first teensy-top-xy)
;;                     (- (second teensy-top-xy) 1)
;;                     (/ (+ 6 teensy-width) 2)])
;;            ))


;; Usb Holder
;; (def usb-holder-position (key-position 1 0 (map + (wall-locate2 0 1) [0 (/ mount-height 2) 0])))
;; (def usb-holder-size [6.5 10.0 13.6])
;; (def usb-holder-thickness 4)
;; (def usb-holder-hole
;;     (->> (apply cube usb-holder-size)
;;          (translate [(first usb-holder-position) (second usb-holder-position) (/ (+ (last usb-holder-size) usb-holder-thickness) 2)])))
;;
;;
