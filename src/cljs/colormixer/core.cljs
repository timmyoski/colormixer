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


(defn blend!nn-all [state app-state weight-prim-color]
  (doall
      (for [n (range (count (:board app-state)))]
        (blend!nn state app-state n weight-prim-color))))

(defn map-f-board [a-function mapping-loc a-board]
  (vec
    (map
         #(assoc-in % [mapping-loc] (vec (map a-function (mapping-loc %))))
          a-board)))

;; RESET/SUB-CURSORS????????????
;; (defn map-f-board2 [a-function mapping-loc a-board]
;;   (vec
;;     (map
;;      #(assoc-in % [mapping-loc]
;;                     (vec (map a-function (mapping-loc %))))
;;      a-board)))

;; (defn blender [block] ;;output new-color
;;   (let [board (:board @app-state)
;;         neighbors-colors (map #(:color (board %)) (vals (:neighbors block)))
;;         weight-prim-color 10]
;;     (apply avg-colors (weight-by weight-prim-color (:color block) neighbors-colors))))

;; (blender b ex-board)

;; (map #(blender % ex-board) ex-board)

(defn blender [block board] ;;output new-color
  (let [;; board ex-board
        ;; test-city (prn (:color block))
        neighbors-colors (map #(:color (board %)) (vals (:neighbors block)))
        weight-prim-color 10]
    (apply avg-colors (weight-by weight-prim-color (:color block) neighbors-colors))))


(defn blender-board [a-board]
 (vec
    (map #(assoc-in % [:color] (blender % a-board)) a-board)))


(defn inc-ed-board [board] ;; lighten entire board
  (map-f-board inc :color board))

(defn dec-ed-board
  "returns an entire board with all color vals decremented"
  [board]
  (map-f-board dec :color board))

;; (defn get-incer-board [amt board] ;; lighten entire board
;;   (map-f-board #(+ amt %) :color board))

(defn inc-amt-board [amt board] ;; returns an entire board lightened by amt
  (map-f-board #(+ % amt) :color board))

;; (defn inc-amt-board2 [amt board-cur] ;; returns an entire board lightened by amt
;;   (map-f-board2 #(+ % amt) :color @board-cur))

(defn dec-amt-board [amt board] ;; returns an entire board lightened by amt
  (map-f-board #(- % amt) :color board))


;; (defn dec-ed-board
;;   "returns an entire board with all color vals decremented"
;;   [board]
;;   (map-f-board dec :color board))

;;; here 9/3 hmmm seems to be working right...?  can't swap out entire board w/o rerender entire app huh tho?


;; (defn blended-board [board]
;;   (map-f-board #() :color board))

;;TOTALLY LEGIT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
;; (symbol (str "r" "r")) => rr (as a var!!!)


(defn run-key-f [a-key state e board-cur]
    ((get-in @state [:ctrl-panel :keyboard a-key :f-pressed]) state e board-cur))

(defn get-key-ctrl [a-key a-code a-function]  ;; how solve gen(just-1) prob instaed of change whoe interface?  like just to test and insert in one key-val what output?  map?  like a '(this thing) ?
  {a-key {:code a-code :key a-key :pressed false
          :f-pressed (fn [state e board-cur] ;;auto-gifted these args/refs...
                         (let [view-info (meta @board-cur)
                               new-board (with-meta (a-function @board-cur) view-info)] ;; if using this do this somewhere else
                                 (do (prn "got to the " a-key " f() from in ctrl-panel")
                                     (reset! board-cur new-board))))}})

;; (def m-ctrl (get-key-ctrl "m" "KeyM" (dec-amt-board 5 (r/cursor app-state [:board]))))

(defn init-ctrl-panel []
  {:mouse {:1 {:pressed false}}
   :keyboard {
              " "  {:code "Space" :key " " :pressed false
                    :f-pressed (fn [state e board-cur]
                                 (let [old-board @board-cur
                                       ;; testing (prn "got to test-blend from the key [" (.-code e) "] in ctrl-panel")
                                       new-board (with-meta (blender-board old-board) (meta old-board))] ;; if using this do this somewhere else
                                          (reset! board-cur new-board)))}
;;                     :f-pressed (fn [state e board-cur]  ;; leave a-key included? ;; if already derefed/accessed var val  ;; efficient to just keep passing it?
;;                                  (let [new-board ()]

;;                                       (do
;;                                         (prn "got to function in [" (.-code e) "] in ctrl-panel" "---> it blends the entire board")
;;                                         (blend!nn-all state @state 10))))}

              "b"  {:code "KeyB" :key "b" :pressed false
                    :f-pressed (fn [state e board-cur]
                                 (run-key-f " " state e board-cur))}

;;                :f-pressed (fn [ & args ] ;;state e board-cur]
;;                                  (run-key-f " " args))}

              "."  {:code "Period" :key "." :pressed false
                    :f-pressed (fn [state e board-cur]
                                 (let [view-info (meta @board-cur)
                                       new-board (with-meta (inc-ed-board @board-cur) view-info)] ;; if using this do this somewhere else
                                         (do (prn "got to lighten from the key [" (.-code e) "] in ctrl-panel" "---> it lightens the entire board")
                                             (reset! board-cur new-board))))}

              ","  {:code "Comma" :key "," :pressed false
                    :f-pressed (fn [state e board-cur]
                                 (let [view-info (meta @board-cur)
                                       new-board (with-meta (dec-ed-board @board-cur) view-info)] ;; if using this do this somewhere else
                                         (do (prn "got to darken from the key [" (.-code e) "] in ctrl-panel" "---> it lightens the entire board")
                                             (reset! board-cur new-board))))}
              "m"  {:code "KeyM" :key "m" :pressed false
                    :f-pressed (fn [state e board-cur]
                                 (let [old-board @board-cur ;; testing (prn "got to dec5 from the key [" (.-code e) "] in ctrl-panel")
                                       new-board (with-meta (dec-amt-board 5 old-board) (meta old-board))] ;; if using this do this somewhere else
                                          (reset! board-cur new-board)))}

              "/"  {:code "Slash" :key "/" :pressed false
                    :f-pressed (fn [state e board-cur]
                                 (let [old-board @board-cur ;; testing (prn "got to dec5 from the key [" (.-code e) "] in ctrl-panel")
                                       new-board (with-meta (inc-amt-board 5 old-board) (meta old-board))] ;; if using this do this somewhere else
                                          (reset! board-cur new-board)))}

               "t"  {:code "KeyT" :key "t" :pressed false
                    :f-pressed (fn [state e board-cur]
                                 (let [old-board @board-cur
                                       ;; testing (prn "got to test-blend from the key [" (.-code e) "] in ctrl-panel")
                                       new-board (with-meta (blender-board old-board) (meta old-board))] ;; if using this do this somewhere else
                                          (reset! board-cur new-board)))}

               ;; "v" "KeyV"

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


(defn get-block-view-model2 [board-dimensions board-len margin];; DELETED APP WIDTH app-width
  (let [block-total-size  (/ board-len (:width board-dimensions))] ;; (int (/ app-width (:width board-dimensions)))
    {:margin margin
     :block-total-size block-total-size
     :block-size (- block-total-size (* margin 2))}))

(defn init-app-state [board-dimensions app-width-percent];DEFONCE?????
  (let [w-dim {:width (.-innerWidth js/window)
               :height (.-innerHeight js/window)}
        rectangularity (/ (:width w-dim) (:height w-dim))
        long-side-len (max (:width w-dim) (:height w-dim))
        board-len (if (< .8 rectangularity 1.25)
                    (* .8 long-side-len);; if board-len will take up more than 80% of the screen
                    (min (:width w-dim) (:height w-dim)))
        short-long-per (* (/ board-len long-side-len) 100)
        board-percent (/ board-len long-side-len)
        gui-percent (/ (- 100 board-percent) 2)
        ;;blk-size (/ board-len (:width board-dimensions))
        margins 0
        ;; app-"height" (max (:width w-dim) (:height w-dim))
        ;; board-width (- app-height (* 2 gui-height))
        ;; block-size (/ board-width (:width board-dimensions))
        block-view-model2 (get-block-view-model2 board-dimensions board-len margins)
        ;;
        ;;
        ;; by "app" i mean "board" right?
        app-view {:width (:width w-dim)
                  :height (* (/ (- 100 (* 2 gui-percent)) 100) (:height w-dim))}
        board-height board-len
        block-view-model (get-block-view-model board-dimensions app-width-percent board-height margins)
        board-width board-len ;;(* (:width board-dimensions) (:block-total-size block-view-model)) ;; (:width app-view) ;; needed here bc of old api
        ]
    (r/atom
      {:title "...blend away your troubles...."
       :window-dim w-dim
       :background-color [255 255 255]
       :weighted-color [255 255 255];NECCESSARY/WANTED??!?!?!
       :board-width board-width
       :board-height board-height
       :board (with-meta (new-board-refactor board-dimensions)
                         {:board-dimensions board-dimensions :screen-percent app-width-percent})
       :board-dimensions board-dimensions
       :app-width-percent app-width-percent
       :block-view-model block-view-model2 ;; (get-block-view-model board-dimensions app-width-percent app-width app-height margins)
       :ctrl-panel (init-ctrl-panel)
       :view {:gui {:width (* (/ 100 100) (:width w-dim))
                    :height (* (/ gui-percent 100) (:height w-dim))}
              :app {:width board-width
                    :height board-height}
              :modal {:future true}
              :block {:future true}}})))



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
           :on-mouse-move (fn [e] (do  ;;(.preventDefault e "false");stops text/mouse highlighting
                                       (blend!nn state app-state n 7)))
                              ;; sep out blend and set functionality for more complex behavior later?
                              ;; attach this function to block-model like OOP?
           :on-mouse-down (fn [e] (do
                                 (swap! state assoc-in [:board n :color] weighted-color)))}]))
                                 ;(swap! state assoc-in [:board n :mutable] false)))}]))

(defn render-board [state app-state]
  (let [board-dimensions (:board-dimensions app-state)
        block-view-model (:block-view-model app-state)
        board-width (:board-width app-state) ;;75%
        board-height (:board-height app-state)]
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ;; am here
    ;; how do i get the blocks to function with css %??? haha easy
            [:div {:class "board"
                   :id "board"
                   :style {:width board-width :height board-height}}
                (doall (for [n (range (* (:height board-dimensions)
                                         (:width board-dimensions)))]
                            (render-block-html state app-state block-view-model n)))]))

;;proper names for these -> render render-rgb-input?
(defn render-rgb-input [color-type weighted-color-cor];onkeypress='return event.charCode >= 48 && event.charCode <= 57'>
  (let [rgb ["red" "green" "blue"] ;onenter-> sw focus to next?
        rgb-index (.indexOf rgb color-type)]
    ;add some sort of button that has a :on method that incs/decs the color input ;just have it dec the val, start that chain of derrefs/renders
      [:input {;;:class "rgb-input"
               :type "text"
               :max-length 3 ;; :on-key-press (fn [e] (if (< 47 (.-keyCode e) 58) (do (prn (.-keyCode e)) e)))
               :name color-type
               :placeholder color-type
               :style {;;:border-color color-type
                       :text-align "center"
                       :border (str "2px solid" " " color-type)
                       :width "20vw"
                       :height "5vh"
                       :margin "0 5vw 0 5vw"
                        :flex 1
                        :align-self "center"}
               :on-change (fn [e] (swap! weighted-color-cor assoc-in [rgb-index]
                                                                     (int (.-value (.-target e)))))}]))

;; /* top | right | bottom | left */
;; margin: 2px 1em 0 auto;

;; .rgb-input{
;;   width: 15%;
;;   height: 35px;
;;   text-align: center;
;;   border: 5px solid;
;; }


;; (defn make-keyword [ & inputs ]
;;   (keyword (apply str inputs)))
;; (make-keyword [4] "g" 5)
;; (make-keyword "div" "." "top-gui-wrapper")

(defn render-bottom-gui [state app-state]
  (let [weighted-color-cor (r/cursor state [:weighted-color])]

      [:div.bottom-gui-wrapper {:style {:display "flex" ;;:width "20em" :height "2em"  ;; <--how to functionize? (str ? ;;;;;;;;;; ;; [:div {:class "title-wrapper"} ;; [:h2 {:class "the-title"} (:title app-state)]]]
                                       :background-color (rgb-str (:weighted-color @state))
                                       :align-items "flex-end"
                                       :justify-content "bottom"
                                       :width "100vw"
                                       :height "10vh"
                                       :border-color "blue"}}
         ;;[:div ;;{:class "input-wrapper"}
;;             [:div {;;:class "input-gui"
;;                    :style {:background-color (rgb-str @weighted-color-cor)}}
                      (render-rgb-input "red" weighted-color-cor)
                      (render-rgb-input "green" weighted-color-cor)
                      (render-rgb-input "blue" weighted-color-cor)
    ;; stoped here 9/3 trying to make reset button work
        ;((get-in @state [:ctrl-panel :keyboard "r" :f-pressed]) state e (r/cursor state [:board]))))}
;;        [:img {:src "/images/favicon.ico" ;;:class "loading-img"
;;               :style {:width "5%"
;;                       :visibility "hidden"}}]
             ]))

(defn render-top-gui [state app-state]
  (let [gui-view (get-in app-state [:view :gui])
        board-cur (r/cursor state [:board])] ;; HAHA MAKE THIS ACTUALLY WORK
      [:div.top-gui-wrapper {:style {:display "flex" ;;:width "20em" :height "2em"
                                     :background-color (rgb-str (:weighted-color @state))
                                     :align-items "center"
                                     :justify-content "center"
                                     :width "100%"
                                     :height "10vh"}}
       [:div {:class "dec"
              :style {:background-color (rgb-str (map #(+ % 30) (:weighted-color @state)))
                      :font-size "5rem"
                      :width "25%"
                      :height "100%"
                      :display "flex" ;;:width "20em" :height "2em"
                      :align-items "center"
                      :justify-content "center"}
                         ;; :height "2em"} ;; come back to real css solution to % width/height
;;                  :on-mouse-down (fn [e] (do
;;                                             (prn "this works mouse-down")
;;                                             ((get-in @state [:ctrl-panel :keyboard "m" :f-pressed]) state e board-cur)))
              :on-touch-start (fn [e] (do
                                          (prn "this works touch-start in reset")
                                          ((get-in @state [:ctrl-panel :keyboard "m" :f-pressed]) state e board-cur)))}
      [:p "-"]]
      [:div {:class "reset"
             :style {:background-color (rgb-str (:weighted-color @state))
                     :font-size "2rem"
                     :width "50%"}
                     ;; :height "2em"} ;; come back to real css solution to % width/height
;;                  :on-mouse-down (fn [e] (do
;;                                             (prn "this works mouse-down")
;;                                             ((get-in @state [:ctrl-panel :keyboard "r" :f-pressed]) state e board-cur)))
             :on-touch-start (fn [e] (do
                                        (prn "this works touch-start in reset")
                                        ((get-in @state [:ctrl-panel :keyboard "r" :f-pressed]) state e board-cur)))}
             "reset"]
       [:div {:class "inc"
             :style {:background-color (rgb-str (map #(+ % 30) (:weighted-color @state)))
                     :font-size "4rem"
                     :width "25%"
                     :height "100%"
                     :display "flex" ;;:width "20em" :height "2em"
                     :align-items "center"
                     :justify-content "center"}
                     ;; :height "2em"} ;; come back to real css solution to % width/height ???
;;                  :on-mouse-down (fn [e] (do
;;                                             (prn "this works mouse-down")
;;                                             ((get-in @state [:ctrl-panel :keyboard "/" :f-pressed]) state e board-cur)))
             :on-touch-start (fn [e] (do
                                        (prn "this works touch-start in reset")
                                        ((get-in @state [:ctrl-panel :keyboard "/" :f-pressed]) state e board-cur)))}
             "+"]]))

;;"function (state,e,board_cur){


;;                  (fn [e state]
;;                                      (let [board-cur (r/cursor state [:board])
;;                                            view-info (meta @board-cur) ;; is this the new $$$$ jackpot?  view/model info as meta (view is always meta?)  (globals/constants always meta?)
;;                                            board-dim (:board-dimensions view-info)
;;                                            new-board (with-meta (new-board-refactor board-dim) view-info)]
;;                                               (do  (prn "got to function in [ lors"))))} ;;(.-code e) "] in click event" "---> it resets the board-colors"))))}
;;                                                    ;; (reset! board-cur new-board))))}

;; :view {:gui {:width (* (/ 100 100) (:width w-dim))
;;                     :height (* (/ gui-height-per 100) (:height w-dim))}
;;               :app {:width board-width
;;                     :height board-height}


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
;;
;; all get run whenever window/app is mouse-down
;; individual handlers get called whenever that element is mouse-down

(defn mouse-handler [state e]
  (if (= (.-type e) "mousedown")
    (prn "reached mouse down handler"
         (.-button e)
         (.-buttons e)
         (.-relatedTarget e)
         (.-type e))))

;; (fn [e] (do
;;     (prn "this works touch-start in reset")
;;     ((get-in @state [:ctrl-panel :keyboard "r" :f-pressed]) state e board-cur)))

;;9/6 get rid of doble tap zoom


;; The tap event is not native, because it relies on conditionally listening upon touchstart to determine if the start and stop targets are the same: if so, jQuery Mobiel determines that it is indeed a genuine tap event and fires the custom, non-native event tap. This logic can be seen in the original source file, at line 75 onwards of ./js/events/touch.js.

;; An example usage is as follow:

;; $(selector).on('tap', function(e){
;;     e.preventDefault();
;; });

;; (def temp-tap false) (nil? var)


;; (let [mystr "no match"]
;;   (case mystr
;;         "" 0
;;         "hello" (count mystr)
;;         "default"))
;; ;;=> "default"


;; if

;; handler - ctrl - panel

;; (def ctrl-panel2
;;   {"touchstart {}

;; (defn dispatch-touch [event state]
;;     ((get-in [(.-type e) (.-taget e) :f-pressed]) event state)

(defn touch-event-handler [state e] ;; should be e state to map to js args
  (let [event-type (.-type e)]
    (prn event-type)))

;; touch-start
;; t-pressed = true

(defn touch-start-handler [state e target t-class]
  ;; (let [test-city (prn (js-keys e) " test city touch-start-handler")]
  (prn "hitting touch start handler" target t-class)
  (cond
      (= t-class "colorbox") ((get-in @state [:ctrl-panel :keyboard "t" :f-pressed]) state e (r/cursor state [:board]))
    ))

(defn touch-end-handler [state e target t-class]
  ;; (let [test-city (prn (js-keys e) " test city touch-start-handler")]
  (prn "hitting touch start handler" target t-class)
  (cond
      (= t-class "colorbox") ((get-in @state [:ctrl-panel :keyboard "t" :f-pressed]) state e (r/cursor state [:board]))
    ))
(def start-target nil)

;; let [board-width (min (.-innerWidth .-innerHeight))
;; so the width is always a certain % of the screen based on the rectangularity of the screen
;; can do
;; let [rectagularity (/ .-innerWidth .-innerHeight)]
;; this basically does the same as the modular block t screen ratio idea



(defn touch-handler [state e]
  (let [event-type (.-type e)
        target (.-target e)
        t-class (.-className target)
        ;; test-city (prn (.-type e) "the type???")
        ]
    (cond
      (= (.-type e) "touchstart") (touch-start-handler state e target t-class)
      (= (.-type e) "touchend") (touch-end-handler state e target t-class))))

;;                                 (do
;;                                     (touch-end-handler state e target t-class)
;;                                     (tap-handler state e target t-class)))
        ;;(.preventDefault e) ;; is this fucking doing anything

(defn key-handler [state e]
  (let [;; all of this logic happens twice b/c of key-up and down....
        a-key (.-key e)
        ;; test-mode (prn a-key (.-code e))
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
  (let  ;; listner-add {"keydown" (fn [args] (key-handler args))} ???
    [app (.getElementById js/document "app")] ;; global dumb var
      (do
          ;; (prn app "this is a potentially useless app ref in register-all-listeners")
          (.addEventListener js/window "keydown" (fn [e] (key-handler state e))) ;;THIS IS FUCKED HOW I SIWTCH AROUND THE API HERE SHOULD BE "E" FIRST THEN "STATE"
          (.addEventListener js/window "keyup" (fn [e] (key-handler state e)))
          ;; (.addEventListener js/window "mousedown" (fn [e] (mouse-handler state e)))
          ;; (.addEventListener js/window "mouseup" (fn [e] (mouse-handler state e)))
          (.addEventListener js/window "touchstart" (fn [e] (touch-handler state e))))))
          ;; (.addEventListener js/window "touchmove" (fn [e] (touch-handler state e))))))

;;       (.addEventListener board "touchstart" (fn [e] (do  (prn "hitting touchstart") ;;(.preventDefault e "false")
;;                                                             (blend!nn-all state @state 5))))
;;       (.addEventListener board "touchmove" (fn [e] (do (prn "hittin touchmove")
;;                                                        (swap! state assoc-in [:board (int (.-id (.-target e))) :color]
;;                                                                              (avg-colors (get-in @state [:weighted-color])
;;                                                                                          (get-in @state [:board (int (.-id (.-target e))) :color])))))))))

;; window.scrollTo(0,1);

(defonce load-listeners
    (fn [state] (.addEventListener js/window "load" (register-all-listeners state))
                (.addEventListener js/window "load" (.scrollTo js/window 0 1))))

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

;;------------------------------------------------------------------------------------
;;------------------------------------------------------------------------------------
;; Well it’s nice to end with some good news. In both platforms, in all conditions, @media device-width always returns the same value as screen.width, and device-height always returns the same value as screen.height. There’s no matchMedia support in Opera, but I’ll assume the same would be true.

;; This is great – but of course don’t forget the main issue afflicting those measurements: the lack of rotated values in iOS and Android 2.3’s wacky 800 viewport value which I’ll postulate may be echoed here too.

;; @media width and height also track other JavaScript properties in both platforms. @media width is almost always equal to window.innerWidth (except in non-constrained landscape mode in iOS v5 when the former is 980 and the former that curious 981). @media height is always equal to window.innerHeight.

;; (It’s not so clear whether this pattern would be followed by Opera too, were we able to measure it this way. To confirm, it would require a CSS-based test harness – perhaps another study.)

;; But there are a couple of things worth saying about this observation. Firstly, the window.innerWidth and window.innerHeight values themselves are something of a bit of a mixed bag. See the earlier discussions above.


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
