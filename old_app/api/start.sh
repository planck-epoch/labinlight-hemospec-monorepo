#!/bin/sh
set -e

case $WORKER in

  # YES
  [Yy][Ee][Ss] )
    bundle exec sidekiq
    ;;

  # HYBRID
  [Hh][Yy][Bb][Rr][Ii][Dd] )
    bundle exec sidekiq &
    bundle exec rails server
    ;;

  # ANYTHING ELSE
  *)
    bundle exec rails server
    ;;
esac
