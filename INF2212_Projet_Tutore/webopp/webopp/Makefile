APP := webopp


TARGET := $(APP).dwar 

#  
all: dwar


#  Remove the dwar
clean:
	rm -f $(TARGET) 


#  Babelify and browserify the app (to be executable by old browsers)
build:
	@echo "*** Browserify $(APP) ***"
	cd  HTML5Application && grunt build

# Create the dwar file
dwar: clean build
	cp -r HTML5Application/public_html $(APP); \
	dwar.sh $(APP); \
	\rm -r $(APP)
	@echo "*** $(TARGET) created ***"

# Upload the dwar file 
upload: dwar
	scp $(TARGET) casa-irisa.univ-ubs.fr:/home/casa/www/entrepot/dwar/
