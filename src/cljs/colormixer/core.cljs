(ns colormixer.core
    (:require [reagent.core :as r]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]))


;;---------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;; start

(defn rand-color-num []
  [(rand-int 256)
   (rand-int 256)
   (rand-int 256)]
  ;[255 255 255]
)

(defn px-str [anumb]
  (str anumb "px"))

(defn rgb-str [[r g b]]
  (str "rgba(" r ", " g ", " b ", .90)"))

(defn get-neighbors [n board-dimensions]
  (let [height (:height board-dimensions)
        width (:width board-dimensions)
        last-index (- (* width height) 1)
        rules-vals {:left {:rule (not= (mod n width) 0);if not on left side
                           :val (- n 1)}
                    :right {:rule (not= (mod n width) (- width 1));if not on right side
                            :val (+ n 1)}
                    :up {:rule (> n (- width 1));if not on top
                         :val (- n width)}
                    :down {:rule (< n (- last-index width));if not on bottom
                           :val (+ n width)}}]
      (into {}
            (for [[dir entry] rules-vals
                 :when (:rule entry)]
                    [dir (:val entry)]))))


(defn block-model [n board-dimensions];board is made of n block-models
  {:color (rand-color-num)
   :mutable true
   :neighbors (get-neighbors n board-dimensions)
   :blend-directions ["left" "right" "up" "down"]})

; @app-state :board-dimensions {:width 5 :height 5}
(defn new-board-refactor [board-dimensions] ;should be (:board-dimensions @app-state) ;; (new-board-refactor {:width 5 :height 5})
  (vec (doall (for [n (range (* (:width board-dimensions) (:height board-dimensions)))]
    (block-model n board-dimensions)))))


;;only info that relates to block size (dependant on screen size dynamically)
(defn get-block-view-model [board-dimensions screen-percent app-width margin];app-width
  (let [block-total-size (int (/ app-width (:width board-dimensions)))]
    {:margin margin
     :block-total-size block-total-size
     :block-size (- block-total-size (* margin 2))}))

(defn set-cursor-path! [a-cursor path set-val]
  (swap! a-cursor assoc-in path set-val))


;;              (get-key-ctrl (:key spacebar) (:name spacebar) (:function spacebar))}))


;; every key needs to get registered with a "on key down" = pressed
;;                     aaaaaand a on key up = not-pressed                                 pressed on like windwo load or something, or just have it happen no event just do (on init)
;;  this is the add event listener to all


;; then onkeypress calls "key F()" <- is while loop already in there? (yes bc whole point is to not have it be called by tons of events
;; but by the lack up letting up on the key (mismatched ctrl scheme but they output the same)


(defn new-weighted-avg [ primary-color weight & [color-vecs] ];;unused
  (let [num-other-colors (count color-vecs)
        num-primary (* weight num-other-colors)
        denom (float (+ num-other-colors num-primary))]
   (vec (map +
              (map #(* (/ num-primary denom) %) primary-color); num-color-vecs x weight (amt to overvalue original value in
              (map #(* (/ 1 denom) %) (apply map + color-vecs))))))


(defn avg-colors [ & color-vecs ] ;;if supply 1 val vecs ([255]) or 2 val vecs ([255 255]) will return avg of only r or rg (no b) respectively
  (vec (map #(int (/ % (count color-vecs))) (vec (apply map + color-vecs)))));;;;;;;;;JANKY INT rounding

(defn weight-by [n-times prim-color color-vecs];;returns a new [vec of [co lor vecs]] with ratio of
  (vec (concat (repeat (* n-times (count color-vecs)) prim-color);; bcolor/ncolor = n
               color-vecs)))

(defn blend!nn [state app-state n weight-prim-color]
  (let [;block-model-cor (r/cursor state [:board n]);this like creates crazy recursive calls i hope?
        board (:board app-state) ;neighbors-colors-index (vals (:neighbors block))
        block (board n)
        neighbors-colors (map #(:color (board %)) (vals (:neighbors block)))
        new-color (apply avg-colors (weight-by weight-prim-color (:color block) neighbors-colors))]
           (if (:mutable block)
                  (swap! state assoc-in [:board n :color] new-color))));;ratio of "move"/"blend-all!"? change weight-prim-color val?

;; (defn get-blended-color [primary-color prim-col-weight & color-vecs ]
;;   (let []
;;     (apply avg-colors (weight-by weight-prim-color (:color block) neighbors-colors))))

(defn blend!nn-all [state app-state weight-prim-color]
  (doall
      (for [n (range (count (:board app-state)))]
        (blend!nn state app-state n weight-prim-color)
          )))


(defn init-ctrl-panel []
  {:mouse {:1 {:pressed false}}
   :keyboard {" "  {:name "spacebar"
                    :key " "
                    :pressed false
;;                     :f-pressed (fn [state app-state e a-key]
;;                                    (while (get-in state [:input :keyboard a-key :pressed])
;;                                      (blend!nn-all state app-state 5)))
                    :ff (fn [state app-state e a-key]
                            (do
                              (prn "got to function in spacebar" state app-state e a-key)
                              (blend!nn-all state app-state 5)))}}})

(defn init-app-state [board-dimensions screen-percent];DEFONCE?????
  (let [app-width (* screen-percent (.-innerHeight js/window))
        margins 0]; 1000?????;(.-innerHeight js/window) (:screen-percent @app-state))
    (r/atom
      {:title "...blend away your troubles...."
       :background-color [255 255 255]
       :weighted-color [255 255 255];NECCESSARY/WANTED??!?!?!
       :app-width app-width
       :board (new-board-refactor board-dimensions)
       :board-dimensions board-dimensions
       :block-view-model (get-block-view-model board-dimensions screen-percent app-width margins)
       :input (init-ctrl-panel)})))

(defn render-block-html [state app-state block-view-model n] ;;still called by init
  (let [weighted-color (:weighted-color app-state)
        block-model-cor (r/cursor app-state [:board n])]
    ^{:key n}
    [:div {:class "colorbox"
           :id n ;; ?????r-uuid?
           :style {:background-color (rgb-str (:color ((:board @state) n)));; ....it waaaaas a DEREF
                   :margin (px-str (:margin block-view-model));;bvm is already der@ffed!
                   :width (px-str (:block-size block-view-model))
                   :height (px-str (:block-size block-view-model))}
           :on-mouse-move (fn [e] (do  (.preventDefault e "false");stops text/mouse highlighting
                                       (blend!nn state app-state n 10)));HARDCODED VAR--------;(prn (js-keys (.-style (.-target e))));(swap! app-state assoc-in [:background-color] (rgb-str (get-in @state [:background-color])))
                               ;sep out blend and set functionality for more complex behavior later?
                               ;attach this function to block-model like OOP?
           :on-mouse-down (fn [e] (do
                                 (swap! state assoc-in [:board n :color] weighted-color)
                                 (swap! state assoc-in [:board n :mutable] false)))}]))

(defn render-app [state app-state]
  (let [board-dimensions (:board-dimensions app-state)
        block-view-model (:block-view-model app-state)
        app-width (:app-width app-state)]

            [:div {:class "content"
                   :style {:width app-width}}
                (doall (for [n (range (* (:height board-dimensions)
                                         (:width board-dimensions)))]
                            (render-block-html state app-state block-view-model n)))]))

;;proper names for these -> render render-rgb-input?
(defn render-rgb-input [color-type weighted-color-cor];onkeypress='return event.charCode >= 48 && event.charCode <= 57'>
  (let [rgb ["red" "green" "blue"] ;onenter-> sw focus to next?
        rgb-index (.indexOf rgb color-type)]
    ;add some sort of button that has a :on method that incs/decs the color input ;just have it dec the val, start that chain of derrefs/renders
      [:input {:class "rgb-input"
               :type "text"
               :max-length 3 ;; :on-key-press (fn [e] (if (< 47 (.-keyCode e) 58) (do (prn (.-keyCode e)) e)))
               :name color-type
               :placeholder color-type
               :style {:border-color color-type}
               :on-change (fn [e] (swap! weighted-color-cor assoc-in [rgb-index]
                                                                     (int (.-value (.-target e)))))}]))

(defn render-gui [state app-state]
  (let [weighted-color-cor (r/cursor state [:weighted-color])]

      [:div {:class "gui-wrapper"}
        [:div {:class "title-wrapper"}]
            ;[:h2 {:class "the-title"} (:title app-state)]]
        [:div {:class "inputs"}
            [:div {:class "submit-button"
                   :style {:background-color (rgb-str @weighted-color-cor)}}
                  (render-rgb-input "red" weighted-color-cor)
                  (render-rgb-input "green" weighted-color-cor)
                  (render-rgb-input "blue" weighted-color-cor)]]]))

(defn render-colormix [state]
  (let [app-state @state]
        [:div {:class "react-container"}
          (render-gui state app-state)
          (render-app state app-state)]))

(defn key-handlers [e state]
  (let [app-state @state]
    (do
        (prn (.-key e) "key of keyup" (js-keys e) (.-key e)))
        (cond
          (= (.-key e) " ") (blend!nn-all state app-state 10); SpaceBar ;;stop propigation() AAAAAND stopDefault()?
          (= (.-key e) "g") (doall (for [i (range 10)] (blend!nn-all state app-state 10)));; g do blend 10x
          ;(= (.-keyCode e) 78) (all-immutable num-tiles);n - make not mutable
          ;(= (.-keyCode e) 77) (swap! state assoc-in [:board] (vec (map #((:mutable %) (:board app-state)))));m - make mutable
          ;(= (.-keyCode e) 188) (swap! state assoc-in [:board] (vec (map #(vec (map inc %)) (map #(:color %) (:board app-state)))))
        )))

; with map structure of inputs key-handlers can adopt a function/lookup syntax
;; (.-keyCode e) = 56 (int)
;; (get-in app-state [:input :keyboard (.-keyCode e) :function])

;; (let [app-state @state
;;       key (.-key e)
;;       key-model-cursor (r/cursor state [:input :keyboard key])]
;;
;;       ((:function-tree @key-model-cursor) state app-state e key)
;;

;;((get-in input [:input :keyboard 32 :function]) "hey stranger") - working

;; (defn update-content-width []
;;   (swap! app-state assoc-in [:width] (* (.-innerHeight js/window) (/ 75 100.0)))
;;   (swap! app-state assoc-in [:block] (get-block num-tiles)))

;; (doall (for [a-key ctrl-panel]
;;                  (.addEventListener js/window (:name a-key) (get-in a-key [:functions (:name a-key)])

;; orrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr  refactor and make it so
;;
;; (for [events evt-holder]
;;
;; target.addEventListener(type, listener[, options]); or (.addEventListener js/window type listener)
;; only care about mouse/key events right tho? (down/up?)

;;  (.addEventListener js/window "keyup" (fn [e] (key-up-handlers state app-state e)))
;;  (.addEventListener js/window "keydown" (fn [e] (key-down-handlers state app-state e)))
;;  (.addEventListener js/window "mouseup" (fn [e] (mouse-up-handlers state app-state e)))
;;  (.addEventListener js/window "mousedown" (fn [e] (mouse-down-handlers state app-state e)))

;; generalize this code?
;; : on-down (do (swap! pressed true) (run code))
;; ; on-up (swap pressed false)

;; (defn event {:when "on"
;;              :what "mouse"
;;              :action "move"})

;(defn add-event-listener [target event

;; on-pressed has an atom/watcher
;; on-change -> test truthy -> while truthy run code
;; makes all "buttons" reactive now to press (will animate blending!!!!)




(defn add-listeners [state]
  (do (.addEventListener js/window "keyup" (fn [e] (key-handlers e state)))
  ;(.addEventListener js/window "onresize" update-content-width)
))

(defn mouse-handler [state e]
  (prn (js-keys e) "reached mouse handler wheysef"))

;; (defn map-blend [a-block];cursor?


(defn blend-all-final! [state app-state weight-prim-color]
  ;(map #(swap! state (:color %) map-blend (:board app-state))
  (prn "hmmm")
)

(defn key-handler [state e]
  (let [a-key (.-key e)
    key-cursor (r/cursor state [:input :keyboard a-key])]
    (if (= "keydown" (.-type e))
      (cond ;"function (state,app_state,e,a_key){
        (= a-key " ") ((:ff @key-cursor) state @state e a-key) ;((get-in @state [:input :keyboard a-key :ff]) state 10)
        ;:else (prn "type is dooooown : " (.-type e) " ;-(")
      ))))
      ;(do
      ;  (prn "type is uppppppppppppp : " (.-type e) " ;-)")))))
;;     (if (:pressed
;;     (do
;;         (swap! state assoc-in [:input :keyboard a-key :pressed] true)
;;         ())))
;;touchstart touchend touchmove

(defn register-all-listeners [state]
  (let [app (.getElementById js/document "app")]
  (do
      (prn app)
      (.addEventListener js/window "keydown" (fn [e] (key-handler state e)))
      (.addEventListener js/window "keydown" (fn [e] (key-handler state e)))
      (.addEventListener js/window "keyup" (fn [e] (key-handler state e)))
      (.addEventListener js/window "mousedown" (fn [e] (mouse-handler state e)))
      (.addEventListener js/window "mouseup" (fn [e] (mouse-handler state e)))
      (.addEventListener js/window "touchstart" (fn [e] (blend!nn-all state @state 5)))
      (.addEventListener js/window "touchmove" (fn [e] (swap! state assoc-in [:board (int (.-id (.-target e))) :color]
                                                                             (avg-colors (get-in @state [:weighted-color])
                                                                                         (get-in @state [:board (int (.-id (.-target e))) :color]))))))))

(defn load-listeners [state]
    (.addEventListener js/window "load" (register-all-listeners state)))

(def board-dimensions {:width 9 :height 9})
(def screen-percent (/ 80 100.0))
(def app-state (init-app-state board-dimensions screen-percent))

;; (defn init-init []
;;   (let [board-dimensions {:width 9 :height 9}
;;         screen-percent (/ 75 100.0)]
;;         ;init-app-state (init-app-state board-dimensions screen-percent)]
;;           (do
;;             (load-listeners app-state)
;;             ;(add-listeners app-state)
;;             (render-colormix app-state);init-app-state)
;;     )))
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------

;; -------------------------
;; Views

(defn home-page []
  (let [board-dimensions {:width 9 :height 9}
        screen-percent (/ 75 100.0)]

    (render-colormix app-state)))
;;   [:div [:h2 "Welcome to colormixer"]
;;    [:div [:a {:href "/about"} "go to about page"]]])
;;      (init-init))

(defn about-page []
  [:div [:h2 "About colormixer"]
   [:div [:a {:href "/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (do
    (load-listeners app-state)
    (r/render [current-page] (.getElementById js/document "app"))))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
