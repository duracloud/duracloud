function loadPlaylistPlayer(streamHost, playlistFile) {
    var so = new SWFObject('player.swf','ply','710','390','9','#ffffff');
    so.addParam('allowfullscreen','true');
    so.addParam('allowscriptaccess','always');
    so.addParam('wmode','opaque');
    so.addParam('flashvars','playlistfile='+playlistFile+'&playlist=right&backcolor=111111&frontcolor=ffffff&streamer=rtmp://'+streamHost+'/cfx/st');
    so.write('mediaspace');
}

function loadSinglePlayer(streamHost, videoFile) {
    var so = new SWFObject('player.swf','ply','470','290','9','#ffffff');
    so.addParam('allowfullscreen','true');
    so.addParam('allowscriptaccess','always');
    so.addParam('wmode','opaque');
    so.addVariable('skin','stylish.swf');
    so.addVariable('file', videoFile);
    so.addVariable('streamer', 'rtmp://' + streamHost + '/cfx/st');
    so.write('mediaspace');
}