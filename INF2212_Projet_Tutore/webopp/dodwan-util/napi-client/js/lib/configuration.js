/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
console.log("Start module configuration.js");
  
    
 /*
 * Class ConfigurationService ---------------------------------------------
 */
class ConfigurationService {
    

    constructor() {
      this.config = {
        "server_host":"localhost",
        "server_port":"8025",
        "reconnect_period":"15",
        "reconnect_nb_tries_max":"5"      
      };
    }
    
    
    init(conf) {
        this.config = { ...this.config, ...conf};
    }
        

    
}



// Singleton
export var service =  new ConfigurationService();

