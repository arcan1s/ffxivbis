.PHONY: check clean compile dist push tests version
.DEFAULT_GOAL := compile

PROJECT := ffxivbis

check:
	sbt scalafmtCheck

clean:
	sbt clean

compile: clean
	sbt compile

format:
	sbt scalafmt

dist: tests
	sbt dist

push: version dist
	git add version.sbt
	git commit -m "Release $(VERSION)"
	git tag "$(VERSION)"
	git push
	git push --tags

tests: compile check
	sbt test

version:
ifndef VERSION
	$(error VERSION is required, but not set)
endif
	sed -i '/version := "[0-9.]*/s/[^"][^)]*/version := "$(VERSION)"/' version.sbt
