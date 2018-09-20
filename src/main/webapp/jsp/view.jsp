<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
	<meta name="apple-mobile-web-app-status-bar-style" content="black">
	
	<link rel="stylesheet" href="css/hcf.css">
	<link rel="stylesheet" href="css/view.css">
	<link media="handheld, only screen and (max-device-width: 480px)" rel="stylesheet" href="css/handheld.css">
</head>
<body>

<%
String cam = (String) request.getAttribute("cam");
String t = (String) request.getAttribute("t");
%>

<div id="video">
	<img id="video-element" alt="Stream" src="cam?t=<%= t %>&cam=<%= cam  %>">
</div>

<script type="text/javascript" src="js/jquery.js"></script>

<script type="text/javascript">

	var overlay = false;
	
	$('#video-element').click( function () {
		if (!overlay) {
			$('#video-options').fadeTo('fast', 0.7);
		}else{
			$('#video-options').fadeOut('fast');
		}
		
		overlay = !overlay;	
	});
	
	// change the view size according to device orientation
	window.onorientationchange = function () {
		var o = window.orientation;
		var e = $('#video-element');
		var w = $(window);
		var d = $(document);
		
		d.width( w.width() );
		d.height( w.height() );
		
		if (Math.abs(o) != 90) { //portrait
			e.css({width:'100%', height:'auto'});
			
		}else{ // landscape
			//e.css('width', 'auto').height(d.height());
			e.css({width:'100%', height:'100%'});
			//height(d.height() + 120).width(d.width());
		}
		
		window.fullscreen();
	}
	
	// set the view fullscreen when flipped
	window.fullscreen = function () {
		
		setTimeout( function() {
			
			if (Math.abs(window.orientation) == 90) {
				window.scrollTo(0, 120);
			}
			
		}, 100);
	};
	
	$(document).ready(
		window.onorientationchange
	);
</script>

</body>
</html>
