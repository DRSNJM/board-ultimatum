DOCS_BRANCH=gh-pages
SEPARATOR=============================================================================
BOOTSTRAP_DIR=resources/bootstrap/
BOOTSTRAP_MAKE=make -C ${BOOTSTRAP_DIR}
ECHO=@/usr/bin/env echo -e


# Call bootstrap's bootstrap task and copy the results into public.
bootstrap:
	$(ECHO) $(SEPARATOR)
	$(ECHO) Making with bootstrap...
	$(ECHO) ${SEPARATOR}
	${BOOTSTRAP_MAKE} bootstrap
	$(ECHO)
	$(ECHO) ${SEPARATOR}
	$(ECHO) Copying bootstrap generated assets into resources/public/
	$(ECHO) ${SEPARATOR}
	cp -R ${BOOTSTRAP_DIR}bootstrap/* resources/public/
	$(ECHO)
	$(ECHO) ${SEPARATOR}
	$(ECHO) Cleaning up...
	$(ECHO) ${SEPARATOR}
	${BOOTSTRAP_MAKE} clean

# Quickly regenerate less.  For development use only.
quickbs:
	recess --compile ${BOOTSTRAP_DIR}less/board-ultimatum.less > resources/public/css/bootstrap.min.css

watch:
	$(ECHO) Watching less files...; \

	watchr -e "watch('${BOOTSTRAP_DIR}less/.*\.less') { system 'make quickbs' }"

# Generate documentation using marginalia
marg:
	$(ECHO) ${SEPARATOR}
	$(ECHO) "Using marginalia to create documentation. . ."
	$(ECHO) ${SEPARATOR}
	@-mkdir -p docs/
	lein marg -d docs/ -f index.html

# Copy generated docs into gh-pages branch
prepare_docs: marg
	$(ECHO) ${SEPARATOR}
	$(ECHO) "Preparing marginalia docs in gh-pages branch. . ."
	$(ECHO) ${SEPARATOR}
	@-mkdir -p .git/_deploy/
	rm -rf .git/_deploy/*
	cp docs/* .git/_deploy/
	git checkout gh-pages
	cp .git/_deploy/* .
	-git commit -am "Update documentation."
	@git checkout - > /dev/null
	$(ECHO)

# Deploy prepared documents
deploy_docs: prepare_docs
	$(ECHO) ${SEPARATOR}
	$(ECHO) "Attempting deployment to origin's ${DOCS_BRANCH} branch."
	$(ECHO) ${SEPARATOR}
	git push -u origin ${DOCS_BRANCH}:${DOCS_BRANCH}

# Should only be run once to make the gh-pages branch.
init_docs:
	$(ECHO)
	$(ECHO) ${SEPARATOR}
	$(ECHO) "Initializing orphan ${DOCS_BRANCH} branch. . ."
	$(ECHO) ${SEPARATOR}
	git checkout --orphan ${DOCS_BRANCH}
	g rm -rf .
	rm -rf docs target Makefile
	touch index.html
	git add index.html
	$(ECHO)
	$(ECHO) "\tAttempting an initial commit. . ."
	$(ECHO)
	git commit -m "Initial commit."

deps:
	$(ECHO) "Get dependencies for building bootstrap."
	sudo npm install recess connect uglify-js jshint -g
