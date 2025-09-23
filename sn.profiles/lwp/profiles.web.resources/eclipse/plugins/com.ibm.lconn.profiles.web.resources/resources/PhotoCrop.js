/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Corp. 2001, 2022                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

dojo.provide("lconn.profiles.PhotoCrop");
dojo.require("dojo.io.iframe");
dojo.require("lconn.core.upload.util.UploadUtil");

/**********************************************************************
	Design pattern note:
	If document.onmousemove is set to a method (such as foo.mouseMove), one would
	expect "this" to be equal to "foo" when the mousemove event fires.  Since it is
	not, I have had to use a singleton design pattern.  Also, as JS will only look
	in the function scope and globally for variables, instance variables and
	method calls on the same object must be prefaced with "this".  These two
	problems result in odd-looking code and have necessitated the use of a
	variation on the singleton design pattern.
*/

//---------------[ BEGIN CROPPER DIMENSIONS CLASS ]
lconn.profiles.PhotoCrop.Cropper = function(cropBox) {

	// State variables
	this.drag = new Object();
	this.oldPos;
	this.mouseStart;
	this.crop = true;

	// Page and implementation variables.
	this.containerEdges = null;
	this.boxParent = null;
	this.correctionFactor; //correct for rendering differences on some browsers
	this.isDebug = false;

	// Cropper elements
	this.divCropper = null; // the outermost div container supplied by the object instantiator

	this.div1 = null; // container of all the cropper elements
	this.img1 = null; // the darker image
	this.cDiv = null; // the cropper box (same as this.box)
	this.cImg = null; // the lighter image viewed through the cropper box

	this.nwDiv = null; // nw handle
	this.neDiv = null; // ne handle
	this.swDiv = null; // sw handle
	this.seDiv = null; // se handle

	this.box = null; // Div representing the cropper box to move

	// Constants  (some older JS implementations don't like 'const')
	this.RESIZABLE_EDGE_SIZE = 3;
	this.CORNER_SIZE = 15;
	this.FRAME_SIZE = {x:300, y:300}; // size of the preview frame
	this.MIN_DIMENSION = 25;
	this.FORCE_SQUARE = true;
	this.CROPPER = {
		x:155, // cropper width
		y:155, // cropper height
		top: ((this.FRAME_SIZE.y - 155)/2), // distance from top
		left: ((this.FRAME_SIZE.x - 155)/2), // distance from left
		border: { active:{ width:this.RESIZABLE_EDGE_SIZE, style:"solid", color:"black" }, inactive:{ width:this.RESIZABLE_EDGE_SIZE, style:"dotted", color:"gray" } }
	};

	this.photoFileName = null;


	this.init = function( divEl, frame, cropper) {

		// Init this object with values passed in if available.  Otherwise, keep defaults.
		if( typeof(frame) == "object" && frame.x && frame.y) {
			this.FRAME_SIZE.x = (frame.x? frame.x: this.FRAME_SIZE.x);
			this.FRAME_SIZE.y = (frame.y? frame.y: this.FRAME_SIZE.y);
		}

		if( typeof(cropper) == "object") {
			this.CROPPER.x = (cropper.x? cropper.x: this.CROPPER.x);
			this.CROPPER.y = (cropper.y? cropper.y: this.CROPPER.y);
			this.CROPPER.top = (cropper.top? cropper.top: this.CROPPER.top);
			this.CROPPER.left = (cropper.left? cropper.left: this.CROPPER.left);
		}

		this.divCropper = dojo.byId(divEl);

		this.div1 = document.createElement("div");
		this.div1.id = "imgPreviewContainer";

		this.img1 = document.createElement("img");
		this.img1.id = "backgroundImg";
		this.img1.src = "";
		this.img1.width= this.FRAME_SIZE.x;
		this.img1.height= this.FRAME_SIZE.y;

		this.cDiv = document.createElement("div");
		this.cDiv.id="cropperBox";

		this.cImg = document.createElement("img");
		this.cImg.id = "cropperImg";
		this.cImg.src = "";
		this.cImg.width=  this.FRAME_SIZE.x;
		this.cImg.height= this.FRAME_SIZE.y;

		this.nwDiv = document.createElement("div");
		this.neDiv = document.createElement("div");
		this.swDiv = document.createElement("div");
		this.seDiv = document.createElement("div");

		this.resetImage(true);

		this.cDiv.appendChild(this.cImg);
		var top = this.CROPPER.top + this.CROPPER.border.inactive.width;
		var left = this.CROPPER.left + this.CROPPER.border.inactive.width;
		dojo.style( this.cImg, {
			position: "absolute",
			top: "-"+top+"px",
			left: "-"+ left +"px"
		});

		this.div1.appendChild(this.img1);
		dojo.style( this.img1, {
			position: "relative",
			display: "block",
			margin: "0px",
			padding: "0px",
			opacity: "1"
		});

		this.div1.appendChild(this.cDiv);
		dojo.style( this.cDiv, {
			left: this.CROPPER.left+"px",
			top: this.CROPPER.top+"px",
			width: this.CROPPER.x+"px",
			height: this.CROPPER.y+"px",
			position: "absolute",
			overflow: "hidden",
			"float": "left",
			padding: "0px",
			"border": this.CROPPER.border.inactive.width + "px " + this.CROPPER.border.inactive.style + " " + this.CROPPER.border.inactive.color,
			cursor: "move"
		});

		this.div1.appendChild(this.nwDiv);
		dojo.style( this.nwDiv, {
			cursor: "nw-resize",
			opacity: "0.5", position: "absolute", width: "0px", height: "0px", background: "#D8DFEA", padding: "0px",margin: "0px", left: "0px", top: "0px"
		});

		this.div1.appendChild(this.neDiv);
		dojo.style( this.neDiv, {
			cursor: "ne-resize",
			opacity: "0.5", position: "absolute", width: "0px", height: "0px", background: "#D8DFEA", padding: "0px",margin: "0px", left: "0px", top: "0px"
		});
		this.div1.appendChild(this.swDiv);
		dojo.style( this.swDiv, {
			cursor: "sw-resize",
			opacity: "0.5", position: "absolute", width: "0px", height: "0px", background: "#D8DFEA", padding: "0px",margin: "0px", left: "0px", top: "0px"
		});

		this.div1.appendChild(this.seDiv);
		dojo.style( this.seDiv, {
			cursor: "se-resize",
			opacity: "0.5", position: "absolute", width: "0px", height: "0px", background: "#D8DFEA", padding: "0px",margin: "0px", left: "0px", top: "0px"
		});

		this.setCorners( this.CROPPER.top, this.CROPPER.left, this.CROPPER.x, this.CROPPER.y );

		this.divCropper.appendChild(this.div1);
		dojo.style( this.div1, {
			position: "relative",
			padding:  "0px",
			background: "black",
			visibility: "hidden",
			margin: "0px auto",
			display: "block",
			zoom: 1,
			width: this.FRAME_SIZE.x+"px",
			height: this.FRAME_SIZE.y+"px"
		});

		this.setBox(this.cDiv);
	},


	// reset cropper and image to be cropped
	this.resetImage = function(resetCropper) {
		this.cImg.src = "";
		this.img1.src = "";

		var tempImgUrl = applicationContext + "/html/tempPhoto.do?resize=true&xsize="+this.FRAME_SIZE.x+"&ysize="+this.FRAME_SIZE.y+"&time=" + (new Date()).getTime();
		this.cImg.src = tempImgUrl;
		this.img1.src = tempImgUrl;

		if(resetCropper) {
			this.crop = false;
			this.active = false;
			dojo.style( this.cDiv, { "border": this.CROPPER.border.inactive.width + "px " + this.CROPPER.border.inactive.style + " " + this.CROPPER.border.inactive.color });
			dojo.style( this.img1, { "opacity": "1" });
		}
	},

	// reset drag variables to false, reset cursor, disable onmousemove
	this.reset = function() {
		this.drag = new Object();
		document.body.style.cursor = 'default';
		//document.onmousemove = null; //stop hogging user's CPU
		document.onmousemove = this.mouseMovePointer;
	},

	this.display = function(){
		dojo.removeClass( this.divCropper, "lotusHidden");
	},

	this.hide = function(){
		dojo.addClass( this.divCropper, "lotusHidden");
	},

	// Some values may be "123" or "123px", handle either condition.
	this.pxToInt = function (numPixels) {
		//Unary + is the fastest string to int conversion in JS
		if( numPixels != "") {
			var reNum = /(\d+)\D*/;
			return +reNum.exec(numPixels)[1];
		}
		else {
			return 0;
		}
	},

	this.getOffset = function getOffset_$0108(element) {
		if (!element) {
			return {
				top: 0,
				left: 0
			};
		}

	    var rect = element.getBoundingClientRect(),
			vp = element.ownerDocument.defaultView,
			offset = {
				top: rect.top + vp.pageYOffset,
				left: rect.left + vp.pageXOffset
			};
		if (this.debug) {
			console.log("-getOffset()", offset);
		}
		return offset;
	},

	this.getContainerEdges = function (element) {
		var retval = this.getOffset(element);

		retval.right = retval.left + this.pxToInt(element.style.width);
		retval.bottom = retval.top + this.pxToInt(element.style.height);
		if (this.debug) {
			console.log("-getContainerEdges()", retval);
		}
		return retval;
	},

	this.getEdges = function (coord) {
		var retval = new Object();
		if (coord.x < this.left() - this.RESIZABLE_EDGE_SIZE ||
				coord.y < this.top() - this.RESIZABLE_EDGE_SIZE ||
				coord.x > this.right() + this.RESIZABLE_EDGE_SIZE ||
				coord.y > this.bottom()+ this.RESIZABLE_EDGE_SIZE ) {
			retval.outside = true;
		} else if (coord.x > this.left() + this.RESIZABLE_EDGE_SIZE &&
				coord.y > this.top() + this.RESIZABLE_EDGE_SIZE &&
				coord.x < this.right() - this.RESIZABLE_EDGE_SIZE &&
				coord.y < this.bottom()- this.RESIZABLE_EDGE_SIZE ) {
			//document.body.style.cursor = 'move';
			retval.move = true;
		} else {
			retval.E = Math.abs(coord.x - this.right()) <= this.RESIZABLE_EDGE_SIZE;
			retval.W = Math.abs(coord.x - this.left()) <= this.RESIZABLE_EDGE_SIZE;
			retval.N = Math.abs(coord.y - this.top()) <= this.RESIZABLE_EDGE_SIZE ||
					  (Math.abs(coord.y - this.top()) < this.CORNER_SIZE && (retval.E || retval.W));
			retval.S = !retval.N && (Math.abs(coord.y - this.bottom()) <= this.RESIZABLE_EDGE_SIZE ||
					  (Math.abs(coord.y - this.bottom()) < this.CORNER_SIZE && (retval.E || retval.W)));
			retval.E = retval.E ||
					  (Math.abs(coord.x - this.right()) < this.CORNER_SIZE &&(retval.N || retval.S));
			retval.W = !retval.E && (retval.W   ||
					  (Math.abs(coord.x - this.left()) < this.CORNER_SIZE &&(retval.N || retval.S)));
		}
		return retval;
	},

	// Return border positions in a usable numeric format.
	this.left = function() {return this.pxToInt(this.box.style.left);},
	this.right = function() {return this.pxToInt(this.box.style.left) + 	this.pxToInt(this.box.offsetWidth);},
	this.top = function() {return this.pxToInt(this.box.style.top);},
	this.bottom = function() {return this.pxToInt(this.box.style.top) + 	this.pxToInt(this.box.offsetHeight);},
	this.styleRight = function() {return this.pxToInt(this.box.style.left) + 	this.pxToInt(this.box.style.width);},
	this.styleBottom = function() {return this.pxToInt(this.box.style.top) + 	this.pxToInt(this.box.style.height);},

	// Handle IE's broken implementation.
	this.mouseCoords = function(ev){
		// handle FF and IE differences, based on webreference tutorial
		ev = ev || window.event; //IE || FF
		var coords;

		if(ev.pageX != null){ //FF
			coords = { x:ev.pageX - this.containerEdges.left, y:ev.pageY - this.containerEdges.top };
			if (this.isDebug) {
				console.debug( "photoCropper:\n"+
						"X: " + ev.pageX + " - " + this.containerEdges.left + " = " + coords.x + "\n" +
						"Y: " + ev.pageY + " - " + this.containerEdges.top  + " = " + coords.y);
				}

		}	else { //IE
			coords = { x:(ev.clientX + document.documentElement.scrollLeft) - (document.documentElement.clientLeft + this.containerEdges.left),
						y:(ev.clientY + document.documentElement.scrollTop)  - (document.documentElement.clientTop + this.containerEdges.top) }

			if (this.isDebug) {
				console.debug( "photoCropper:\n"+
						"X: ("+ev.clientX+" + "+document.documentElement.scrollLeft+") - ("+document.documentElement.clientLeft+" + "+this.containerEdges.left+") = "+coords.x+"\n" +
						"Y: ("+ev.clientY+" + "+document.documentElement.scrollTop +") - ("+document.documentElement.clientTop +" + "+this.containerEdges.top +") = "+coords.y);
				}
		}

		return coords;
	},

	// To prevent excessive CPU usage, this function is only tied to document.onmousemove while actively dragging.
	this.mouseMove = function(ev) {
		var cropper = lconn.profiles.PhotoCrop.getCropper();
		if(	cropper && cropper.active ) {
			if(this.isDebug) console.debug("photoCropper: Mouse Moved");
			return cropper._mouseMove(ev);
		}
	},

	this._mouseMove = function(ev) {

		try {
			ev = ev || window.event; // IE || FF
			var cropper = lconn.profiles.PhotoCrop.getCropper();

			var newCoords = this.mouseCoords(ev);
			var newHeight = curHeight = this.pxToInt(this.box.style.height);
			var newWidth = curWidth = this.pxToInt(this.box.style.width);
			var newLeft = curLeft = this.pxToInt(this.box.style.left);
			var newTop = curTop = this.pxToInt(this.box.style.top);

			if (this.drag.move) {
				// MOVE CROPPER

				// Movement differs enough that it'd be inefficient to attempt to reuse code.
				newTop = this.oldPos.top + (newCoords.y - this.mouseStart.y);// + "px";
				newLeft = this.oldPos.left + (newCoords.x - this.mouseStart.x);//+ "px";

				// ensure we don't get out of our container dimensions
				newTop = (newTop < 0 ? 0 : newTop); //quicker than Math.min()
				newTop = (/*containerEdges.top +*/ newTop > this.boxParent.offsetHeight - this.box.offsetHeight ? this.boxParent.offsetHeight - this.box.offsetHeight : newTop);
				newLeft = (newLeft < 0 ? 0 : newLeft);
				newLeft = (/*containerEdges.left +*/ newLeft > this.boxParent.offsetWidth - this.box.offsetWidth ? this.boxParent.offsetWidth - this.box.offsetWidth : newLeft);

				this.box.style.top = newTop + "px";
				this.box.style.left = newLeft+"px";

				if (document.body.style.cursor != "move") document.body.style.cursor = "move";
			}

			else {
				// RESIZE CROPPER
				if (this.drag.E) {
					if(this.isDebug) console.debug("photoCropper: EAST SIDE DRAG");
					newWidth = (newCoords.x - this.oldPos.left) - (2* this.correctionFactor);

				} else if (this.drag.W) {
					if(this.isDebug) console.debug("photoCropper: WEST SIDE DRAG");
					newLeft = this.oldPos.left + (newCoords.x - this.mouseStart.x);
					if (newLeft < 0) {
						newLeft = 0;
					} else if (this.right() - newLeft < this.MIN_DIMENSION) {
						newLeft = this.right() - this.MIN_DIMENSION;
					}
					newWidth = this.oldPos.width + this.oldPos.left - newLeft;
				}

				if (this.drag.N) {
					if(this.isDebug) console.debug("photoCropper: NORTH SIDE DRAG");
					newTop = this.oldPos.top + (newCoords.y - this.mouseStart.y);
					if (newTop < 0) {
						newTop = 0;
					} else if (this.bottom() - newTop < this.MIN_DIMENSION) {
						newTop = this.bottom() - this.MIN_DIMENSION;
					}
					newHeight = this.oldPos.height + (this.oldPos.top - newTop);
				} else if (this.drag.S) {
					if(this.isDebug) console.debug("photoCropper: SOUTH SIDE DRAG");
					newHeight = (newCoords.y - this.oldPos.top) - (2* this.correctionFactor);

				}

				if (this.FORCE_SQUARE) {
					if(this.isDebug) console.debug("photoCropper: FORCING SQUARE");

					// Choose which of the two dimensions to keep.
					if (!(this.drag.N || this.drag.S)) {
						newHeight = newWidth;
					} else if (!(this.drag.E || this.drag.W)) {
						newWidth = newHeight;
					} else {
						if(this.isDebug) console.debug("photoCropper: DIAGONAL DRAG");
						if (newWidth < newHeight) {
							//choose width
							newDim = newWidth;
							if (this.drag.N) newTop = this.oldPos.top +
								this.oldPos.height - newDim;
						} else {
							//choose height
							newDim = newHeight;
							if (this.drag.W) newLeft = this.oldPos.left +
								this.oldPos.width - newDim;
						}
						newWidth = newHeight = newDim;
					}
				}

				// Ensure that the box isn't too small and that the right & bottom edges
				// are within the parent div.  These two functions (or subroutines, really)
				// are called in a different order depending on how the user is resizing.
				var checkBottom = function(cropper) {
					if (newHeight < cropper.MIN_DIMENSION) {
						if(this.isDebug) console.debug("photoCropper: MIN SOUTH SIZE REACHED");
						newHeight = cropper.MIN_DIMENSION;
						if (this.FORCE_SQUARE) {
							newWidth = newHeight;
							newLeft = curLeft + curWidth - newWidth;
						}
					} else if (newTop + newHeight +  2 * cropper.correctionFactor > cropper.boxParent.offsetHeight) {
						newHeight = cropper.boxParent.offsetHeight - (newTop + 2*cropper.correctionFactor);
						if (cropper.FORCE_SQUARE) {
							newWidth = newHeight;
							newLeft = curLeft + curWidth - newWidth;
						}
					}
				};

				var checkRight = function(cropper) {
				if (newWidth < cropper.MIN_DIMENSION) {
					if(this.isDebug) console.debug("photoCropper: MIN EAST SIZE REACHED");
					newWidth = cropper.MIN_DIMENSION;
					if (cropper.FORCE_SQUARE) {
						newHeight = newWidth;
						newTop = curTop + curHeight - newHeight;
					}
				} else if (newLeft + newWidth +  2*cropper.correctionFactor
							> cropper.boxParent.offsetWidth) {
					if(this.isDebug) console.debug("photoCropper: MAX EAST SIZE REACHED");
					newWidth = cropper.boxParent.offsetWidth -
						(newLeft + 2*cropper.correctionFactor);
					if (cropper.FORCE_SQUARE) {
						newHeight = newWidth;
						newTop = curTop + curHeight - newHeight;
					}
				}};

				if (this.drag.S) {
					checkBottom(this);
					checkRight(this);
				} else {
					checkRight(this);
					checkBottom(this);
				}
				// Correct for the corrections:
				// Ensure that the enforcement of squareness & placement
				// doesn't lead to an unwanted drag.
				if (newLeft != curLeft && newWidth == curWidth) newLeft = curLeft;
				if (newTop != curTop && newHeight == curHeight)	newTop = curTop;

				this.box.style.height = newHeight + "px";
				this.box.style.width = newWidth + "px";
				this.box.style.top = newTop + "px";
				this.box.style.left = newLeft + "px";
			}

			// since the img is underneath the cropper div, we don't want the img to also move with the cropper box, thus
			// keep the img underneath the cropper box positioned relative to the cropper box
			var top = newTop + this.CROPPER.border.active.width;
			var left = newLeft + this.CROPPER.border.active.width;
			this.cImg.style.top = "-"+top+"px";
			this.cImg.style.left = "-"+left+"px";
			this.setCorners( newTop, newLeft, newWidth, newHeight );
		}
		catch( exc ) {
			alert( exc );
		}

		return false;
	},

	this.setCorners = function( top, left, width, height) {
		// offset the corners a bit from the actual corner of the cropper box
		top -= this.RESIZABLE_EDGE_SIZE;
		left -= this.RESIZABLE_EDGE_SIZE;
		dojo.style(this.nwDiv, "left", left +"px");
		dojo.style(this.nwDiv, "top", top +"px");
		dojo.style(this.neDiv, "left", left+width +"px");
		dojo.style(this.neDiv, "top", top +"px");
		dojo.style(this.swDiv, "left", left +"px");
		dojo.style(this.swDiv, "top", top+height +"px");
		dojo.style(this.seDiv, "left", left+width +"px");
		dojo.style(this.seDiv, "top", top+height +"px");
	},

	this.mouseDown = function(ev) {
		var cropper = lconn.profiles.PhotoCrop.getCropper();
		if( cropper ) {
			cropper.beginCrop(ev);
			return cropper._mouseDown(ev);
		}
	},

	this.beginCrop = function(ev) {
		var cropBox = this.cDiv;
		this.crop = true;
		this.active = true;
		dojo.style( this.cDiv, { "border": this.CROPPER.border.active.width + "px " + this.CROPPER.border.active.style + " " + this.CROPPER.border.active.color });
		dojo.style( this.img1, { "opacity": "0.2" });
	},

	this._mouseDown = function(ev) {
		ev = ev || window.event; //IE || FF
		//if (containerEdges === null) {
			this.containerEdges = this.getContainerEdges(this.boxParent);
		//}
		//find where the user clicked
		this.mouseStart = this.mouseCoords(ev);
		this.drag = this.getEdges(this.mouseStart);
		if(this.isDebug) {
			console.debug("photoCropper:\n"+ "X: " + this.mouseStart.x + " Y: " + this.mouseStart.y);
		}
		this.oldPos = new lconn.profiles.PhotoCrop.BoxPosition(
			this.pxToInt(this.box.style.left),
			this.pxToInt(this.box.style.top),
			this.pxToInt(this.box.style.width),
			this.pxToInt(this.box.style.height));
		document.onmousemove = this.mouseMove;
		this.boxParent.onmousemove = null;
		return false;
	},

	//lightweight mouse move function
	this.mouseMovePointer = function(ev) {
		var cropper = lconn.profiles.PhotoCrop.getCropper();
		if( cropper && cropper.active ) {
			if(this.isDebug) console.debug("photoCropper: Mouse Move Pointer");
			return cropper._mouseMovePointer(ev);
		}
	},

	this._mouseMovePointer = function(ev) {
		ev = ev || window.event; //IE || FF
		var coord = this.mouseCoords(ev);
		mousePos = this.getEdges(coord);
		if (mousePos.E || mousePos.W || mousePos.N || mousePos.S) {
			document.body.style.cursor = (mousePos.N?"n":"") + (mousePos.S?"s":"") + (mousePos.E?"e":"") + (mousePos.W?"w":"") + "-resize";
		} else if (document.body.style.cursor != 'default'){
			document.body.style.cursor = 'default';
		}
	},

	//stop moving
	this.mouseUp = function(ev) {
		var cropper = lconn.profiles.PhotoCrop.getCropper();
		if( cropper )
			return cropper._mouseUp(ev);
	},

	this._mouseUp = function(ev) {
		ev = ev || window.event; //IE || FF
		if (ev.type === "mouseup" ) { //prevent spurious events
			this.reset();
			this.mouseMovePointer(ev);
		}
		return true;
	},

	//stop dragging when the mouse is moved off of the document
	this.mouseOut = function(ev) {
		var cropper = lconn.profiles.PhotoCrop.getCropper();
		if( cropper )
			return cropper._mouseOut(ev);
	},

	this._mouseOut = function(ev) {
		ev = ev || window.event; //IE || FF
		src = ev.srcElement || ev.target;
		if (src.nodeName === "BODY" || src.nodeName === "HTML"
			|| src.id === "imagePreview") { // let go of anything we're dragging
			this.reset();
		}
	},

	//Set the div to move & resize, and initialize its values.
	this.setBox = function(div) {
		this.box = div;
		this.box.onmousedown = this.mouseDown;
		this.box.style.top = this.CROPPER.top;
		this.box.style.left = this.CROPPER.left;
		this.box.style.width = this.CROPPER.x;
		this.box.style.height = this.CROPPER.y;

		//this.box.style.position = 'relative';
		if (this.boxParent == null) this.boxParent = this.box.parentNode;
		this.boxParent.onmouseout = this.mouseUp;
	},

	// Return an array containing {leftX, rightX, topY, bottomY)
	this.getRectCoords = function() {
		return { startX:left() , endX:right() - (2*correctionFactor),
				startY:top(), endY:bottom()- (2*correctionFactor)};
	},

	this.getRelativeCoords = function() {
		var w = this.pxToInt(this.boxParent.style.width);
		var h = this.pxToInt(this.boxParent.style.height);
		return { startX:(this.left() + this.correctionFactor) / w,
				endX:(this.right() - this.correctionFactor) / w,
				startY:(this.top() + this.correctionFactor) / h,
				endY:(this.bottom()- this.correctionFactor) / h };
	},

-	//init the seelction box
	this.init(cropBox);
	this.containerEdges = this.getContainerEdges(this.boxParent);

	//set up normal listeners; these are on all the time
	this.boxParent.onmousedown = this.mouseDown;
	document.onmouseup = this.mouseUp;
	document.onmouseout = this.mouseOut;
	this.boxParent.onmouseout = this.mouseOut;

	//correct for differences in rendering
	this.correctionFactor = ((this.box.offsetHeight - this.pxToInt(this.box.style.height)) / 2)

	if(this.isDebug) {
		console.debug("photoCropper: boxParent.id: " + this.boxParent.id);
		console.debug("photoCropper: containerEdges.left: " + this.containerEdges.left);
		console.debug("photoCropper: boxParent.offsetHeight: " + this.boxParent.offsetHeight);
		console.debug("photoCropper: box.offsetTop: " + this.box.offsetTop);
	}

	this.reset();

}; //--------------------[ END CROPPER DIMENSIONS CLASS ]



// Position object
lconn.profiles.PhotoCrop.BoxPosition = function (left, top, width, height) {
	this.left = +left;
	this.top = +top;
	this.width = +width;
	this.height = +height;
};

var cropper = null;
lconn.profiles.PhotoCrop.cropSingleton = null;

lconn.profiles.PhotoCrop.initCropper = function( imgDiv ) {
	if(typeof(imgDiv) != "object") return false;
	lconn.profiles.PhotoCrop.cropSingleton = new lconn.profiles.PhotoCrop.Cropper(imgDiv);
	return lconn.profiles.PhotoCrop.getCropper();
};

lconn.profiles.PhotoCrop.getCropper = function() {
	if (lconn.profiles.PhotoCrop.cropSingleton == null) {
		lconn.profiles.ProfilesCore.showAlert(generalrs["label.editprofile.photo.no_init"]);
	}
	return lconn.profiles.PhotoCrop.cropSingleton;
}






//---------------------------------------------------------------------------------
// Section of helper methods that deal with displaying and uploading the photos
//
lconn.profiles.PhotoCrop.goToMyProfile = function() {
	profiles_goto(applicationContext + '/html/myProfileView.do');
};

lconn.profiles.PhotoCrop.goToEditProfile = function( showSuccess ) {
	profiles_goto(applicationContext + '/html/editMyProfileView.do?tab=photo'+(showSuccess?'&success':'') );
};

lconn.profiles.PhotoCrop.saveNclosePressed = function( buttonEl, gotoUrl ) {
	if(dojo.hasClass(buttonEl,'lotusBtnDisabled')) return;

	lconn.profiles.PhotoCrop.disableSaveButtons();
	lconn.profiles.PhotoCrop.submitImage( false, function(type, data, evt) {
		if( lconn.profiles.PhotoCrop.uploadCallback( type, data, evt)) {
			editProfile_saveForm( buttonEl.form, gotoUrl );
		}
		lconn.profiles.PhotoCrop.enableSaveButtons();
	});
}

lconn.profiles.PhotoCrop.savePressed = function( buttonEl ) {
	if(dojo.hasClass(buttonEl,'lotusBtnDisabled')) return;

	lconn.profiles.PhotoCrop.disableSaveButtons();
	lconn.profiles.PhotoCrop.submitImage( false, function(type, data, evt) {
		if( lconn.profiles.PhotoCrop.uploadCallback( type, data, evt)) {
			editProfile_saveForm( buttonEl.form);
		}
		lconn.profiles.PhotoCrop.enableSaveButtons();
	});
}

lconn.profiles.PhotoCrop.removePressed = function(gotoUrl) {
	// callback if the user selects "ok"
	var cb_ = function() {
		lconn.profiles.xhrPost({
			url: applicationContext + '/html/uploadPhoto.do',
			load: function(data){
				if (gotoUrl) {
					profiles_goto(gotoUrl);
				} else {
					window.location.reload(); // reset the page
					lconn.profiles.PhotoCrop.hide("imgPreviewOutterContainer");
					lconn.profiles.PhotoCrop.reloadPhoto("imgProfilePhoto");
					lconn.profiles.PhotoCrop.reloadPhoto("imgProfilePhotoCurrent");
					if( cropper)
						cropper.resetImage(true);
				}
			},
			error: function(data,ioargs){ editProfile_xhrError(data, ioargs); },
			content: {
				removePhoto: "true"
			},
			// RTC 187144 [EH] Dangerous URL Nonce in Profile Image Remove URL (Profiles/Html/UploadPhoto.do)
			headers: {
				'X-Update-Nonce': lconn.profiles.PhotoCrop.getNonceValue() || ''
			},
			checkAuthHeader: true
		});
	};

	lconn.profiles.ProfilesCore.confirm(generalrs["label.editprofile.photo.removeimage.confirm"], cb_);
};

lconn.profiles.PhotoCrop.uploadCallback = function(type, data, evt) {
	var retCode = true;

	try {
		if (cropper) cropper.active = false; // inactive until verified that there is no error

		if (type == 'error.fileContainsVirus') {
			lconn.profiles.ProfilesCore.showInfoMsg("profileInfoMsgDiv", "error", generalrs["error.fileContainsVirus"]);
			retCode = false;
		}
		else if (type == 'errors.photo.filetype') {
			lconn.profiles.PhotoCrop.hide("imgPreviewLoading");
			lconn.profiles.ProfilesCore.showInfoMsg("profileInfoMsgDiv", "error", generalrs["errors.photo.filetype"]);
			retCode = false;
		}
		else if (type == 'errors.photo.maxfilesize') {
			lconn.profiles.ProfilesCore.showInfoMsg("profileInfoMsgDiv", "error", generalrs["errors.photo.maxfilesize"]);
			retCode = false;
		}
		else if (type == 'error') {
			lconn.profiles.ProfilesCore.showInfoMsg("profileInfoMsgDiv", "error", generalrs["errors.photo.general"]);
			retCode = false;
		}
		else if (type == 'Photo OK' ) {
			// refresh displayed images
			lconn.profiles.PhotoCrop.reloadPhoto("imgProfilePhoto");
			lconn.profiles.PhotoCrop.reloadPhoto("imgProfilePhotoCurrent");
/*
			if( typeof(editProfile_photoUploadSuccessGotoUrl) != "undefined" && editProfile_photoUploadSuccessGotoUrl != "" )
				profiles_goto( editProfile_photoUploadSuccessGotoUrl );
			else
				lconn.profiles.PhotoCrop.goToEditProfile((type == 'Photo OK'));
*/
			retCode = true;
		}

		else if (type.indexOf("Photo Cropped OK") != -1) {
			// refresh displayed images
			lconn.profiles.PhotoCrop.reloadPhoto("imgProfilePhoto");
			lconn.profiles.PhotoCrop.reloadPhoto("imgProfilePhotoCurrent");

			// resend the image in order to recreate the temp image on the server, in case we need to recrop (once cropped, the temp img on the server is destroyed)
			lconn.profiles.PhotoCrop.previewImage();
			retCode = true;
		}

		else if (type.indexOf("Temp Photo OK") != -1) {
			if( !cropper)
				cropper = lconn.profiles.PhotoCrop.initCropper( dojo.byId("imgPreview") ); // instantiate a cropper with the uploaded image
			else
				cropper.resetImage(true);

			// check for completion of image upload and download into preview container
			var imgCompleted = window.setInterval(
					function(){
						if( dojo.byId("backgroundImg") != null && dojo.byId("backgroundImg").complete ) {
							window.clearInterval(imgCompleted);
							cropper.active = true;
							dojo.style("imgPreviewContainer","visibility","visible");
							lconn.profiles.PhotoCrop.hide("imgPreviewLoading");
							lconn.profiles.PhotoCrop.enableSaveButtons();
						}
					},
				500);

			retCode = true;

		} else { // unknown reponse code
			lconn.profiles.ProfilesCore.showInfoMsg("profileInfoMsgDiv", "error", generalrs["errors.photo.general"]);
			retCode = false;
		}

	} catch(e) {
		alert(e);
	}

	return( retCode ); // true=no error, false=there was an error
}

// Get & return the value of the dangerousurlnonce field from the form
// RTC 187143 [EH] Dangerous URL Nonce in Profile Image Crop URL (Profiles/Html/UploadPhoto.do)
// RTC 187144 [EH] Dangerous URL Nonce in Profile Image Remove URL (Profiles/Html/UploadPhoto.do)
lconn.profiles.PhotoCrop.getNonceValue = function() {
	var nonceValue = '';
	try {
		nonceValue = dojo.byId('editProfile')['dangerousurlnonce'].value
	} catch(e) {}
	return nonceValue;
}

lconn.profiles.PhotoCrop.submitImage = function( isTemp, callback ){
	try {
		gbDataSaved = true;

		var args = {
			checkAuthHeader: true,
			preventCache: true,
			url: applicationContext + '/html/uploadPhoto.do?key=' + profilesData.displayedUser.key,
			form: dojo.byId('editProfile'),
			load: function( response ) {
				var data = response;
				if (window.gatekeeperConfig && window.gatekeeperConfig['image_auto_rotate_enable']) {
					var dom = dojo.toDom(data);
					data = dom.textContent.trim();
				}
				if (typeof(callback) == "function") {
					callback(data, {});
				} else {
					lconn.profiles.PhotoCrop.uploadCallback(data, {});
				}
			},
			error: function( err ) {
				editProfile_xhrError(data, ioargs||{});
			}
		};

		if (dojo.getObject("com.ibm.ajax.auth")) {
			com.ibm.ajax.auth.prepareSecure(args);
		}

		if (isTemp) {
			args.content = { temp: "true" }; // create img as a temp instead of saving permanent into profile
		}

		if (cropper && cropper.crop) { // send the coordinates to crop and save the previously uploaded image
			var c = cropper.getRelativeCoords();
			// RTC 187143 [EH] Dangerous URL Nonce in Profile Image Crop URL (Profiles/Html/UploadPhoto.do)
			args.headers = {
				'X-Update-Nonce': lconn.profiles.PhotoCrop.getNonceValue() || ''
			};

			args.content = dojo.mixin(args.content, {
				subEditForm: dojo.byId('editProfile')['subEditForm'].value,
				crop: true, startx:c.startX, endx:c.endX, starty:c.startY, endy: c.endY
			});

			delete(args.form); // remove the form from the submit since dojo will add fields from it as URL params
			lconn.profiles.xhrPost( args );

		} else { // upload the image
			var dfd = new dojo.Deferred();
			var promise = dfd.promise;
			if (window.gatekeeperConfig && window.gatekeeperConfig['image_auto_rotate_enable']) {
				var uploadUtil = new lconn.core.upload.util.UploadUtil();
				var fileInputEl = dojo.byId('photoUploadFileSelector');
				var file = fileInputEl && fileInputEl.files && fileInputEl.files.length == 1 ? fileInputEl.files[0] : null;
				uploadUtil.convertImageOrientation(file).then(
					dojo.hitch(this, function(data) {
					var fileTextEl = dojo.byId('photoUploadFileSelected');
					fileTextEl.value = lconn.profiles.ProfilesCore.getUploadFileName(fileInputEl);
					if (data && data.opts && !data.opts.isOrigin) {
						var targetForm = dojo.clone(dojo.byId('editProfile'));
						var inputs = targetForm.getElementsByTagName('input');
						if (inputs.photoUploadFileSelector) {
							inputs.photoUploadFileSelector.parentNode.removeChild(inputs.photoUploadFileSelector);
						}
	
						var uploadFormData = new FormData(targetForm);
						uploadFormData.append("photo", data.file);
						if (isTemp) {
							uploadFormData.append('temp', "true");
						}
						
						args.handleAs = 'text';
						args.headers = {
							'X-Update-Nonce': lconn.profiles.PhotoCrop.getNonceValue() || '',
						}
						args.url = applicationContext + '/html/uploadPhoto.do?key=' + profilesData.displayedUser.key;
						args.rawBody = uploadFormData;
						delete(args.form);
						lconn.profiles.xhrPost( args );
						return dfd.resolve();
					} else {
						return dfd.reject();
					}
				})).otherwise(function(){
					return dfd.reject();
				});
			} else {
				dfd.reject();
			}

			promise.otherwise(dojo.hitch(this, function() {
				if (typeof require === "function") {
					args.data = args.content;
					require(["dojo/request/iframe"], function(iframe){
						iframe(args.url, args).then(args.load, args.error);
					});
				} else {
					dojo.io.frame.send(args);
				}
			}));
		}
	}
	catch(e) {
		alert(e);
	}
}

// updates the src of an img element with a lastMod set to current time so as to force a refresh of the img element
lconn.profiles.PhotoCrop.reloadPhoto = function( el) {
	if( !el) return;
	var photo = dojo.byId(el);
	if( photo && photo.src) {
		photo.src += ( photo.src.indexOf("&") == -1? "?" : "&") +"lastMod="+(new Date()).getTime();
	}
}

lconn.profiles.PhotoCrop.show = function( el) {
	if(!el) return;
	var el = dojo.byId(el);
	if(el) dojo.removeClass(el,"lotusHidden");
}

lconn.profiles.PhotoCrop.hide = function( el) {
	if(!el) return;
	var el = dojo.byId(el);
	if(el) dojo.addClass(el,"lotusHidden");
}

lconn.profiles.PhotoCrop.previewImage = function(elFile) {
	if( elFile && elFile.value == "") { // no image file selected (or canceled out of file chooser)
		// reset the preview image
		lconn.profiles.PhotoCrop.disableSaveButtons( );
		lconn.profiles.PhotoCrop.hide("imgPreviewOutterContainer");
		lconn.profiles.PhotoCrop.hide("imgPreviewLoading");
		if(cropper) { // if we had instantiated the cropper, reset it
			cropper.reset();
			cropper.resetImage(true);
		}
	}
	else {
		lconn.profiles.PhotoCrop.disableSaveButtons( );
		lconn.profiles.PhotoCrop.show("lotusFormFieldRow");
		lconn.profiles.PhotoCrop.show("imgPreviewOutterContainer");
		lconn.profiles.PhotoCrop.show("imgPreviewLoading");

		if(cropper) {
			cropper.allowFormSubmit = false;
			cropper.active = false;
			cropper.crop = false;
		}

		//lconn.profiles.PhotoCrop.uploadPressed();
		lconn.profiles.PhotoCrop.submitImage( true );

		// chosen file changed, reset the image
		if( elFile && cropper && elFile.value != cropper.photoFileName) {
			cropper.photoFileName = elFile.value;
			cropper.resetImage(true);
		} else {
			if(cropper) {
				cropper.allowFormSubmit = true;
				cropper.active = true;
				cropper.crop = true;
			}
		}
	}
}

lconn.profiles.PhotoCrop.enableSaveButtons = function( ) {
	lconn.profiles.PhotoCrop.enableButton("lconn_savePhotoButton");
	lconn.profiles.PhotoCrop.enableButton("lconn_saveNclosePhotoButton");
}

lconn.profiles.PhotoCrop.disableSaveButtons = function( ) {
	lconn.profiles.PhotoCrop.disableButton("lconn_savePhotoButton");
	lconn.profiles.PhotoCrop.disableButton("lconn_saveNclosePhotoButton");
}

lconn.profiles.PhotoCrop.enableButton = function( id ) {
	var el = dojo.byId(id);
	if (el) {
		dojo.removeClass(el, "lotusBtnDisabled");
		dojo.removeAttr(el, "disabled");
	}
}

lconn.profiles.PhotoCrop.disableButton = function( id ) {
	var el = dojo.byId(id);
	if (el) {
		dojo.addClass(el, "lotusBtnDisabled");
		dojo.attr(el, "disabled", "disabled");
	}
}

//----------------------------------------------------------------------------------
// updates the file name text field with the file name from the file input field
// args:
//  fileInputEl - file input element object
lconn.profiles.PhotoCrop.updateFileSelectedTextField = function( fileInputEl ) {
	var fileTextEl = dojo.byId('photoUploadFileSelected');
	if (fileTextEl) {
		fileTextEl.value = lconn.profiles.ProfilesCore.getUploadFileName(fileInputEl);
	}
}

//----------------------------------------------------------------------------------
lconn.profiles.PhotoCrop.invokeFileSelect = function( fileSelectorId ) {
	if( fileSelectorId ) {
		var fileSelectorObj = dojo.byId( fileSelectorId );
		if(fileSelectorObj) fileSelectorObj.click();
	}
}






