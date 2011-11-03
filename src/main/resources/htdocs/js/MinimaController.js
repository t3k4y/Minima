function MinimaController(options) {
	this.client = options.client;
	this.view = options.view;
	
	this.readonly = options.readonly;
	
	var view = this.view;
	var client = this.client;
	
	var notes = new NoteCollection();
	notes.url = options.data_location + '/stories/';
	
	var lists = new ListCollection();
	lists.url = options.data_location + '/lists/';
	
	this.client.onBoard(function(board) {
		lists.add(board.lists);
		notes.add(board.stories);
		
		notes.bind('change', function(note, b, c) {
			// according to documentation this is not necessary
			notes.sort();			
		}, this);
			
		var listsView = new ListCollectionView({
			lists: lists,
			notes: notes,
			readonly: this.readonly
		});
		
		$(window).resize(function() {
	    	listsView.resize($(this).width());
		});
		
		$('#board').append(listsView.render().el);
		
		listsView.resize($(window).width());
				
	}, this);
	
	this.client.onReceiveStory(function(note) {		
		var found = notes.get(note.id);
		if (found) {
			found.set(note);
		}
		else {
			notes.add(note);
		}
		
		var model = notes.get(note.id);
				
		if (found && found.get('list') != model.get('list')) {
			var from = lists.get(found.get('list'));
			var to = lists.get(model.get('list'));
			Minima.notify('Note moved from "' + from.get('name') + '" to "' + to.get('name') + '"', model.get('desc'));
		}
		
		if (!found && !model.get('archived')) {
			Minima.notify('New note', model.get('desc'));
			return;
		}
		
		if (found && !found.get('archived') && model.get('archived')) {
			Minima.notify('Note archived', model.get('desc'));
			return;
		}		
		
	}, this);
	
	this.client.onReceiveList(function(list) {		
		var found = lists.get(list.id);
		if (found) {
			found.set(list);
		} else {
			lists.add(list);
		}
	}, this);
	
	var nModel = new NotificationsCtrlModel();
	var nView = new NotificationsCtrlView({model: nModel});	
	$('#notifications_ctrl').append(nView.render().el);
	
}

MinimaController.prototype.start = function() {
	this.client.connect();
	this.client.loadBoard();
}