BackupListener = function() {

	this.dialogShouldBackup = function(note) {
		location.assign('/hcf/backup');
	};

};