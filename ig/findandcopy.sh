#!/bin/bash

pathstr=$(find /usr/lib/ruby/gems/*/gems/jekyll-*/exe/jekyll)
echo $pathstr
regex='ruby/gems/([0-9].[0-9].[0-9])/gems/(jekyll-[0-9].[0-9].[0-9])/'

if [[ $pathstr =~ $regex ]]
then
    cp /usr/lib/ruby/gems/${BASH_REMATCH[1]}/gems/${BASH_REMATCH[2]}/exe/jekyll /usr/bin
fi
