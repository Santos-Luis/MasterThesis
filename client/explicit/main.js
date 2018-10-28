var SEMPRE_URL = "http://localhost:8400";

var selected_cube = -1;
var selected_position = -1;

var sessionId = window.location.search.substr(1);
sessionId = sessionId.substr(sessionId.indexOf('=')+1)

var initialState;
var randomNrs = [0, 1, 2, 3];


function back() {
  window.location.href="../index.html";
}

function site(cmds) {
  var xmlhttp = new XMLHttpRequest();
  var cmdstr = [];
  for (k in cmds) {
    cmdstr.push(k + '=' + encodeURIComponent(cmds[k]));
  }
  var url = SEMPRE_URL + '/sempre?format=lisp2json&'+cmdstr.join('&');
  console.log(url)
  
  xmlhttp.open("GET", url, true);
  xmlhttp.send(null); 
}

/* 
 * Mark functions handle the action of marking a cube/position on the interface
 * 
 * Enter functions handle the process of pass the user feedback to the server
 *
 * (L.S.)
 */

function markColor(el) {
  var color_vec = ["blueCube", "brownCube", "redCube", "orangeCube"]
  var i;
  for (i = 0; i <= 3; i++) {
    if(document.getElementById(color_vec[randomNrs[i]]) == el) {
      selected_cube = i;
      document.getElementById(color_vec[randomNrs[i]]).style.border = "1px solid blue";
    }
    else
      document.getElementById(color_vec[randomNrs[i]]).style.border = "0px";
  }
}

function enterColorPos() {
  if (selected_cube == -1)
    alert("Please select a color first");
  else if (document.getElementById("maintextarea").value == "")
    alert("Please enter an utterance first");
  else {
    var cmds = {q:"(explicit)", sessionId:sessionId};
    site(cmds);
    sleep(100);
    var cmds2 = {q:"(color-pos)" + initialState, sessionId:sessionId};
    site(cmds2);
    sleep(100);
    var cmds3 = {q:selected_cube + " " + document.getElementById("maintextarea").value, sessionId:sessionId};
    site(cmds3);
    sleep(100);
    var cmds4 = {q:"(normal)", sessionId:sessionId};
    site(cmds4);
    var progressBar = document.getElementById("progressBar");
    progressBar.style.visibility = "visible";
    moveProgress();
    setTimeout(back, 4000);
  }
}

// Progress bar displayed after the user enters feedback (L.S.)
function moveProgress() {
  var elem = document.getElementById("progressBarPerc");
  var width = 0;
  var id = setInterval(frame, 39);
  function frame() {
    if (width >= 100) {
      clearInterval(id);
    } else {
      width++; 
      elem.style.width = width + '%';
    }
  }
}

// In color_position feedback, we want the states to be random (L.S.)
function createRandomStates () {
  var cubes = ["blueCube", "brownCube", "redCube", "orangeCube"];

  for(var j, x, i = randomNrs.length; i; j = parseInt(Math.random() * i), x = randomNrs[--i], randomNrs[i] = randomNrs[j], randomNrs[j] = x);
  initialState = "[[" + randomNrs[0] + "],[" + randomNrs[1] + "],[" + randomNrs[2] + "],[" + randomNrs[3] + "]]";
  
  var i;
  for(i = 0; i < cubes.length; i++) {
    document.getElementById(cubes[i]+"1").src = cubes[randomNrs[i]]+".jpg";
    document.getElementById(cubes[i]+"1").alt = cubes[randomNrs[i]];
    document.getElementById(cubes[i]+"1").id = cubes[randomNrs[i]];
  }

  document.getElementById("cubes").style.visibility = "visible";
}

// Horrible hack (L.S.)
function sleep(miliseconds) {
   var currentTime = new Date().getTime();

   while (currentTime + miliseconds >= new Date().getTime()) {
   }
}



