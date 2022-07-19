#!/bin/bash

# alias setup.create.keyboards="structurePlayground";

# function structurePlayground() {
#   mkdir keyboards;
#   cd keyboards;
#   # for ...
# }


sess=keyboards

#----- mrkabuda
tmux new-session -s $sess -d -n mrkabuda
tmux send-keys -t $sess:mrkabuda "printf '\033]2;%s\033\\' '$1'; '$@';" Enter
tmux send-keys -t $sess:mrkabuda "cd ~/projects/keyboards/mrkabuda" Enter
tmux send-keys -t $sess:mrkabuda "vim" Enter

tmux split-window -v -l 24 -t $sess:mrkabuda
tmux send-keys -t $sess:mrkabuda.2 "cd ~/projects/keyboards/mrkabuda" Enter

#----- dactyl
tmux new-window -t $sess -n dactyl
tmux send-keys -t $sess:dactyl "printf '\033]2;%s\033\\' '$1'; '$@';" Enter
tmux send-keys -t $sess:dactyl "cd ~/projects/keyboards/dactyl-keyboard" Enter
tmux send-keys -t $sess:dactyl "vim" Enter

tmux split-window -v -l 24 -t $sess:dactyl
tmux send-keys -t $sess:dactyl.2 "cd ~/projects/keyboards/dactyl-keyboard" Enter

tmux select-pane -t $sess:dactyl.1

#----- manuform
tmux new-window -t $sess -n manuform
tmux send-keys -t $sess:manuform "printf '\033]2;%s\033\\' '$1'; '$@';" Enter
tmux send-keys -t $sess:manuform "cd ~/projects/keyboards/dactyl-manuform" Enter
tmux send-keys -t $sess:manuform "vim" Enter

tmux split-window -v -l 24 -t $sess:manuform
tmux send-keys -t $sess:manuform.2 "cd ~/projects/keyboards/dactyl-manuform" Enter
tmux select-pane -t $sess:manuform.1

#----- Cherry-Mx-Bitboard-Re
tmux new-window -t $sess -n bitboard
tmux send-keys -t $sess:bitboard "printf '\033]2;%s\033\\' '$1'; '$@';" Enter
tmux send-keys -t $sess:bitboard "cd ~/projects/keyboards/Cherry-Mx-Bitboard-Re" Enter
tmux send-keys -t $sess:bitboard "vim" Enter

tmux split-window -v -l 24 -t $sess:bitboard
tmux send-keys -t $sess:bitboard.2 "cd ~/projects/keyboards/Cherry-Mx-Bitboard-Re" Enter
tmux select-pane -t $sess:bitboard.1

#select first
tmux select-window -t $sess:mrkabuda

tmux -2 attach-session -t $sess


