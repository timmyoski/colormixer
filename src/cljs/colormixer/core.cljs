(ns colormixer.core
    (:require [reagent.core :as r]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]))


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
;;------------------------------------------------------------------------------------

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

;; DELETED APP WIDTH !!!! from args
(defn get-block-view-model [board-dimensions screen-percent app-height margin];; DELETED APP WIDTH app-width
  (let [block-total-size  (/ app-height (:height board-dimensions))] ;; (int (/ app-width (:width board-dimensions)))
    {:margin margin
     :block-total-size block-total-size
     :block-size (- block-total-size (* margin 2))}))


;;block height % app-height etc???

(defn set-cursor-path! [a-cursor path set-val]
  (swap! a-cursor assoc-in path set-val))


;;              (get-key-ctrl (:key spacebar) (:name spacebar) (:function spacebar))}))


;; every key needs to get registered with a "on key down" = pressed
;;                     aaaaaand a on key up = not-pressed                                 pressed on like windwo load or something, or just have it happen no event just do (on init)
;;  this is the add event listener to all


;; then onkeypress calls "key F()" <- is while loop already in there? (yes bc whole point is to not have it be called by tons of events
;; but by the lack up letting up on the key (mismatched ctrl scheme but they output the same)


;;  duuuuuuuuuuuuuuuuuuuuuuuuuuuuuude
;; "unneccessary" mirrored scafolding in view and model b/c....
;; can treat lookups the same (and lookups = speed)
;; i.e. "same" lines of code (APIs match up)
;;  (get-in model path) (get-in view path) same path -- diff var ------>>>>> Ffffffunctionize


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
        (blend!nn state app-state n weight-prim-color))))

(defn map-f-board [a-function block-loc a-board]
  (vec
    (map
     #(assoc-in % [block-loc]
                    (vec (map a-function (block-loc %))))
     a-board)))

;;disassoc fucntion from data structure

(defn inc-ed-color-board [board]
  (map-f-board inc :color board))
;;; here 9/3 hmmm seems to be working right...?  can't swap out entire board w/o rerender entire app huh tho?


;;TOTALLY LEGIT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
;; (symbol (str "r" "r")) => rr (as a var!!!)

(defn init-ctrl-panel []
  {:mouse {:1 {:pressed false}}
   :keyboard {
              " "  {:code "Space" :key " " :pressed false
                    :f-pressed (fn [state e board-cur]  ;; leave a-key included?
                                              ;; if already derefed/accessed var val
                                              ;; efficient to just keep passing it?
                            (do
                              (prn "got to function in [" (.-code e) "] in ctrl-panel" "---> it blends the entire board")
                              (blend!nn-all state @state 10)))}

              "."  {:code "Period" :key "." :pressed false
                    :f-pressed (fn [state e board-cur]
                                 (let [view-info (meta @board-cur)
                                       new-board (with-meta (inc-ed-color-board @board-cur) view-info)] ;; if using this do this somewhere else
                                         (do (prn "got to function in [" (.-code e) "] in ctrl-panel" "---> it lightens the entire board")
                                             (reset! board-cur new-board))))}

              "r" {:code "KeyR" :key "r" :pressed false
                   :f-pressed (fn [state e board-cur]  ;; DO - always just pass round state/global and deref when get approp level --> key-cursor at key-handler
                                (let [view-info (meta @board-cur) ;; is this the new $$$$ jackpot?  view/model info as meta (view is always meta?)  (globals/constants always meta?)
                                      board-dim (:board-dimensions view-info)
                                      new-board (with-meta (new-board-refactor board-dim) view-info)]
                                (do  (prn "got to function in [" (.-code e) "] in ctrl-panel" "---> it resets the board-colors")
                                     (reset! board-cur new-board))))}
              }})


;; multiple row divs (set width implcitly slash there is no width
;; rows in general

;; set some sort of inner-width so that the width is num-block*block-width

;; relative block rectangularity/size to window (solves the orientation problem)

;; rectangles in general

;; app-height isn't 80% but like 50% (weak)

(defn init-app-state [board-dimensions app-width-percent];DEFONCE?????
  (let [window-dim {:width (.-innerWidth js/window)
                    :height (.-innerHeight js/window)}
        gui-height-per 15
        margins 0
        app-view {:width (* 1 (:width window-dim))
                  :height (* (/ (- 100 (* 2 gui-height-per)) 100) (:height window-dim))}
        app-height (:height app-view)
        block-view-model (get-block-view-model board-dimensions app-width-percent app-height margins)
        app-width (* (:width board-dimensions) (:block-total-size block-view-model)) ;; (:width app-view) ;; needed here bc of old api
        ]
    (r/atom
      {:title "...blend away your troubles...."
       :window-dim window-dim
       :background-color [255 255 255]
       :weighted-color [255 255 255];NECCESSARY/WANTED??!?!?!
       :app-width app-width
       :app-height app-height
       :board (with-meta (new-board-refactor board-dimensions)
                         {:board-dimensions board-dimensions :screen-percent app-width-percent})
       :board-dimensions board-dimensions
       :app-width-percent app-width-percent
       :block-view-model block-view-model ;; (get-block-view-model board-dimensions app-width-percent app-width app-height margins)
       :ctrl-panel (init-ctrl-panel)
       :view {:gui {:width (* (/ 100 100) (:width window-dim))
                    :height (* (/ gui-height-per 100) (:height window-dim))}
              :app {:width app-width
                    :height app-height}
              :modal {:future true}}})))



;; whoaaaaaaaaaaaaaaaa duuuuuude
;; objects are just stateful functions
;; f can be made to spit out dynamic objects based on state
;; functional prog uber alles
;; global objects/maps/constants are ok tho, but should be spit out of init functions

;; change to...
;; :board {:board-view-model {:board-dimensions board-dimensions
;;                            :screen-percent screen-percent}
;;         :board-model (new-board-refactor board-dimensions)}
;; this way can pass around @board-cursor and not need anything else to re-render
;; i.e. re-render =? init-app-state? new-board-refactor?

(defn make-cursor [an-atom a-path]
  (r/cursor an-atom a-path))

(defn render-block-html [state app-state block-view-model n] ;;still called by init
  (let [weighted-color (:weighted-color app-state)
        block-model-cor (r/cursor app-state [:board n])]
    ^{:key n}
    [:div {:class "colorbox"
           :id n ;; ?????r-uuid?
           :style {:background-color (rgb-str (:color ((:board @state) n)));; ....it waaaaas a DEREF
                   :margin (px-str (:margin block-view-model));;bvm is already der@ffed!
                   :width  (px-str (:block-size block-view-model)) ;; would (str (/ 75 9) "%") even work?
                   :height (px-str (:block-size block-view-model))}
           :on-mouse-move (fn [e] (do  (.preventDefault e "false");stops text/mouse highlighting
                                       (blend!nn state app-state n 7)))
                              ;; sep out blend and set functionality for more complex behavior later?
                              ;; attach this function to block-model like OOP?
           :on-mouse-down (fn [e] (do
                                 (swap! state assoc-in [:board n :color] weighted-color)))}]))
                                 ;(swap! state assoc-in [:board n :mutable] false)))}]))

(defn render-board [state app-state]
  (let [board-dimensions (:board-dimensions app-state)
        block-view-model (:block-view-model app-state)
        app-width (:app-width app-state) ;;75%
        app-height (:app-height app-state)]
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ;; am here
    ;; how do i get the blocks to function with css %??? haha easy
            [:div {:class "board"
                   :id "board"
                   :style {:width app-width :height app-height}}
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

;; (defn make-keyword [ & inputs ]
;;   (keyword (apply str inputs)))
;; (make-keyword [4] "g" 5)
;; (make-keyword "div" "." "top-gui-wrapper")

(defn render-bottom-gui [state app-state]
  (let [weighted-color-cor (r/cursor state [:weighted-color])]

      [:div.bottom-gui-wrapper ;; <--how to functionize? (str ? ;;;;;;;;;; ;; [:div {:class "title-wrapper"} ;; [:h2 {:class "the-title"} (:title app-state)]]]
        [:div {:class "input-wrapper"}
            [:div {:class "input-gui"
                   :style {:background-color (rgb-str @weighted-color-cor)}}
                      (render-rgb-input "red" weighted-color-cor)
                      (render-rgb-input "green" weighted-color-cor)
                      (render-rgb-input "blue" weighted-color-cor)
    ;; stoped here 9/3 trying to make reset button work
        ;((get-in @state [:ctrl-panel :keyboard "r" :f-pressed]) state e (r/cursor state [:board]))))}

       [:img {:src "/images/favicon.ico" ;;:class "loading-img"
              :style {:width "10%"}}]]]]))

(defn render-top-gui [state app-state]
  (let [gui-view (get-in app-state [:view :gui])
        board-cur (r/cursor state [:board])] ;; HAHA MAKE THIS ACTUALLY WORK
      [:div.top-gui-wrapper {:style {:display "flex" ;;:width "20em" :height "2em"
                                     :background-color (rgb-str (:weighted-color @state))
                                     :align-items "center"
                                     :justify-content "center"
                                     :width "100%"
                                     :height "15vh"}}
          [:div {:class "reset"
                 :style {:background-color (rgb-str (:weighted-color @state))
                         :width "100%"}
                         ;; :height "2em"} ;; come back to real css solution to % width/height
                 :on-mouse-down (fn [e] (do
                                            (prn "this works mouse-down")
                                            ((get-in @state [:ctrl-panel :keyboard "r" :f-pressed]) state e board-cur)))
                 :on-touch-down (fn [e] (do
                                            (prn "this works mouse-down")
                                            ((get-in @state [:ctrl-panel :keyboard "r" :f-pressed]) state e board-cur)))}
                 "reset"]]))

;;"function (state,e,board_cur){


;;                  (fn [e state]
;;                                      (let [board-cur (r/cursor state [:board])
;;                                            view-info (meta @board-cur) ;; is this the new $$$$ jackpot?  view/model info as meta (view is always meta?)  (globals/constants always meta?)
;;                                            board-dim (:board-dimensions view-info)
;;                                            new-board (with-meta (new-board-refactor board-dim) view-info)]
;;                                               (do  (prn "got to function in [ lors"))))} ;;(.-code e) "] in click event" "---> it resets the board-colors"))))}
;;                                                    ;; (reset! board-cur new-board))))}

;; :view {:gui {:width (* (/ 100 100) (:width window-dim))
;;                     :height (* (/ gui-height-per 100) (:height window-dim))}
;;               :app {:width app-width
;;                     :height app-height}


;; (fn [state e board-cur]  ;; DO - always just pass round state/global and deref when get approp level --> key-cursor at key-handler
;;     (let [view-info (meta @board-cur) ;; is this the new $$$$ jackpot?  view/model info as meta (view is always meta?)  (globals/constants always meta?)
;;           board-dim (:board-dimensions view-info)
;;           new-board (with-meta (new-board-refactor board-dim) view-info)]
;;     (do  (prn "got to function in [" (.-code e) "] in ctrl-panel" "---> it resets the board-colors")
;;          (reset! board-cur new-board))))


(defn render-colormix-app [state]
  (let [app-state @state]
        [:div {:class "react-container"}
          (render-top-gui state app-state)
          (render-board state app-state)
          (render-bottom-gui state app-state)]))


;;---------------------------------------------------------------------
;;---------------------------------------------------------------------
;;  Handlers

(defn mouse-handler [state e]
  (if (= (.-type e) "mousedown")
    (prn "reached mouse down handler wheysef"
         (.-button e)
         (.-buttons e)
         (.-relatedTarget e)
         (.-type e))))

(defn touch-handler [state e]
  (if (= (.-type e) "touchdown")
    (prn "reached touch down handler"
;;          (.-button e)
;;          (.-buttons e)
;;          (.-relatedTarget e)
         (.-type e))))

;; (defn run-f-ctrl-panel [element f-path args


(defn key-handler [state e]
  (let [a-key (.-key e)
        key-model-cur (r/cursor state [:ctrl-panel :keyboard a-key])
        f-pressed (:f-pressed @key-model-cur)
        board-cur (r/cursor state [:board])] ;; most/ ???all??? key functions will affect the entire board (unless directional for ctrl "person")
    (if (= "keydown" (.-type e))                              ;; can pass to :f-pressed too
      (f-pressed state e board-cur))))


;; cursors only "work" if you organize code in view/model hierarchy where
;; sub global functions/models/views only can operate on their own "level" (non-global)



 (defn get-f-by-key [a-key ctrl-panel-cur]
   (get-in @ctrl-panel-cur [:keyboard a-key :f-pressed]))

(defn register-all-listeners [state]
  (let
    [app (.getElementById js/document "app")] ;; global dumb var
      (do
          (prn app "this is a potentially uselss app ref in register-all-listeners")
          (.addEventListener js/window "keydown" (fn [e] (key-handler state e)))
          (.addEventListener js/window "keyup" (fn [e] (key-handler state e)))
          (.addEventListener js/window "mousedown" (fn [e] (mouse-handler state e)))
          (.addEventListener js/window "mouseup" (fn [e] (mouse-handler state e)))
          (.addEventListener js/window "touchstart" (fn [e] (touch-handler state e)))
          (.addEventListener js/window "touchmove" (fn [e] (touch-handler state e))))))

;;       (.addEventListener board "touchstart" (fn [e] (do  (prn "hitting touchstart") ;;(.preventDefault e "false")
;;                                                             (blend!nn-all state @state 5))))
;;       (.addEventListener board "touchmove" (fn [e] (do (prn "hittin touchmove")
;;                                                        (swap! state assoc-in [:board (int (.-id (.-target e))) :color]
;;                                                                              (avg-colors (get-in @state [:weighted-color])
;;                                                                                          (get-in @state [:board (int (.-id (.-target e))) :color])))))))))

(defonce load-listeners
    (fn [state] (.addEventListener js/window "load" (register-all-listeners state))))

(def board-dimensions {:width 6 :height 6})
(def screen-percent (/ 80 100.0))
(def app-state (init-app-state board-dimensions screen-percent))

;; -------------------------
;; Views

(defn home-page []
  (let [board-dimensions {:width 9 :height 9}
        screen-percent (/ 75 100.0)]
            (render-colormix-app app-state)))

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
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;; (defn key-handlers [e state]
;;   (let [app-state @state]
;;     (do
;;         (prn (.-key e) "key of key-down" (js-keys e) (.-key e)))
;;         (cond
;;           (= (.-key e) " ") (blend!nn-all state app-state 10); SpaceBar ;;stop propigation() AAAAAND stopDefault()?
;;           (= (.-key e) "g") (doall (for [i (range 10)] (blend!nn-all state app-state 10)));; g do blend 10x
;;           ;(= (.-keyCode e) 78) (all-immutable num-tiles);n - make not mutable
;;           ;(= (.-keyCode e) 77) (swap! state assoc-in [:board] (vec (map #((:mutable %) (:board app-state)))));m - make mutable
;;           ;(= (.-keyCode e) 188) (swap! state assoc-in [:board] (vec (map #(vec (map inc %)) (map #(:color %) (:board app-state)))))
;;         )))


;; (defn add-listeners [state]
;;   (do (.addEventListener js/window "keyup" (fn [e] (key-handlers e state)))
;;   ;(.addEventListener js/window "onresize" update-content-width)
;; ))



;;       (cond
;;         (= a-key " ") ((:f-pressed @key-cur) ;; gets correct f() from ctrl-panel
;;                          state @state e a-key)  ;; args for f() from ctrl-panel
;;         (= a-key ".") ((:f-pressed @key-cur) state @state e a-key)  ;; should i keep structure more general like this ?
;;         (= a-key "r") ((:f-pressed @key-cur) (r/cursor state [:board]) (:board-dimensions @state)) ;; just pass an app-state cursor? most functions will affect the entire board but mouse/etc will affect certain areas (but can pass entire board as fail safe?
        ;; :else (prn a-key "type is dooooown.... type= " (.-type e)  "code " (.-code e) " ;-(")
;;       )))

      ;(do (prn "type is uppppppppppppp : " (.-type e) " ;-)")))))

      ;; (f-pressed args)
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------

;; random notes
;; no mas scroll

; with map structure of inputs key-handlers can adopt a function/lookup syntax
;; (.-keyCode e) = 56 (int)
;; (get-in app-state [:ctrl-panel :keyboard (.-keyCode e) :function])

;; (let [app-state @state
;;       key (.-key e)
;;       key-model-cursor (r/cursor state [:ctrl-panel :keyboard key])]
;;
;;       ((:function-tree @key-model-cursor) state app-state e key)
;;

;;((get-in input [:ctrl-panel :keyboard 32 :function]) "hey stranger") - working

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



;; FLEXIBLE BLOCKS
;; all the flexibility and mobile-compatibilyt can be achieved
;; solely through use of the built-in (% width) of css

;; keep like a gui-width (10%) white/-selected color around app
;; favoicon/home-app-button/modal t/f

;; bottom has like 2-3 buttons (game) functionality
;; home button would toggle t/f of :modal in app-state

;; just have modal work like an atom?
;; flip display css property of 'modal'
