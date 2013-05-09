$(document).ready(function() {
  var ws = new WebSocket("ws://localhost:4714/game/1");

  ws.onopen = function()
  {
    // Web Socket is connected, send data using send()
    console.log("Socket opened!");
  };

  ws.onclose = function()
  { 
    // websocket is closed.
    console.log("Socket closed!");
  };

  function send(msg) {
    ws.send(msg);
  }

  function register_player(name) {
    send(JSON.stringify(
      {'type': 'register-player',
       'name': name}
    ));
  }

  function register_viewer() {
    send(JSON.stringify(
      {'type': 'register-viewer'}
    ));
  }

  ws.onmessage = function (evt) 
  { 
    console.log("Received:", evt.data);
  };
});
