/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/*
module.exports = function (grunt) {
    // Project configuration.
    grunt.initConfig({
    });
};
*/

module.exports = function (grunt) {
   grunt.initConfig({
      browserify: {
         dist: {
            options: {
               transform: [
                  ["babelify", {
                     loose: "all"
                  }]
               ]
            },
            files: {
               "./public_html/dist/webopp.js": ["./public_html/dom.js"]
            }
         }
      },
      watch: {
         scripts: {
            files: ["./public_html/*.js"],
            tasks: ["browserify"]
         }
      }
   });

   grunt.loadNpmTasks("grunt-browserify");
   grunt.loadNpmTasks("grunt-contrib-watch");

   grunt.registerTask("default", ["watch"]);
   grunt.registerTask("build", ["browserify"]);
};
