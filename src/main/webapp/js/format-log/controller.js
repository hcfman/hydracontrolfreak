FormatLogController = function () {
    this.load = function () {
    	JSNotificationCenter.addObserver(this, 'initialise', 'LoadedJSON');
    };
    
    this.initialise = function (note) {
    	var data = note.data;
		JSDialog.setController(this);
        console.log('Initialised Format Log Controller');
        
        var lines = data.log.split('\n');
        lines.reverse();
        
        jQuery('#log').text(lines.join('\n'));
    };
    
    this.load();
};


// singleton
FormatLogController = new FormatLogController();