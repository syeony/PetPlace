(function($, window, document, undefined){
	// adds .naturalWidth() and .naturalHeight() methods to jQuery
	// for retreaving a normalized naturalWidth and naturalHeight.
	// Example usage:
	// var 
	// nWidth = $('img#example').naturalWidth(),
	// nHeight = $('img#example').naturalHeight();
	var
	props = ['Width', 'Height'],
	prop;
	
	while (prop = props.pop()) {
		(function (natural, prop) {
			$.fn[natural] = (natural in new Image()) ? 
			function () {
			return this[0][natural];
			} : 
			function () {
				var 
				node = this[0],
				img,
				value;
				
				if (node.tagName.toLowerCase() === 'img') {
				img = new Image();
				img.src = node.src,
				value = img[prop];
				}
				return value;
			};
		}('natural' + prop, prop.toLowerCase()));
	}
	
	// inline style 적용된것중 특정 css만 제거
	$.fn.removeStyle = function(style){
		var search = new RegExp(style + '[^;]+;?', 'g');

		return this.each(function(){
			$(this).attr('style', function(i, style){
				return style ? style.replace(search, '') : style;
			});
		});
	};
	
	/* 
	 * 
	 * 같은 값이 있는 열을 병합함
	 * 
	 * 사용법 : $('#테이블 ID').rowspan(0);
	 * 
	 */    
	$.fn.rowspan = function(colIdx, isStats) {
		return this.each(function(){
			var that;
			$('tr', this).each(function(row) {
				$('td:eq('+colIdx+')', this).filter(':visible').each(function(col) {
					if ($(this).html() == $(that).html() && (!isStats || isStats && $(this).prev().html() == $(that).prev().html())) {
						rowspan = $(that).attr("rowspan") || 1;
						rowspan = Number(rowspan)+1;
	 
						$(that).attr("rowspan",rowspan);

						// do your action for the colspan cell here
						$(this).hide();
						//$(this).remove(); 
						// do your action for the old cell here
					} else {
						that = this;
					}
					// set the that if not already set
					that = (that == null) ? this : that;
				});
			});
		});
	};
	
	/* 
	 * 
	 * 같은 값이 있는 행을 병합함
	 * 
	 * 사용법 : $('#테이블 ID').colspan (0);
	 * 
	 */  
	$.fn.colspan = function(rowIdx) {
		return this.each(function(){
			var that;
			$('tr', this).filter(":eq("+rowIdx+")").each(function(row) {
				$(this).find('th').filter(':visible').each(function(col) {
					if ($(this).html() == $(that).html()) {
						colspan = $(that).attr("colSpan") || 1;
						colspan = Number(colspan)+1;
						$(that).attr("colSpan",colspan);
						$(this).hide(); // .remove();
					} else {
						that = this;
					}
					// set the that if not already set
					that = (that == null) ? this : that;
				});
			});
		});
	}
	
	/*
	 * 이미지 태그 팝업창으로 보여주기
	 * jquery, jquery-ui 가 있어야함
	 * 
	 */
	$.fn.imgView = function(options){
		var defaults = {
			view_percent : 80			// 이미지 최대 사이즈(화면에 따라서 퍼센트)
		};
		
		// 기본값과 options값을 합침
		var sets = $.extend({}, defaults, options);
		
		// img dim 처리 영역
		var $img_dim = $('<div>', {
			'id' : 'NT_img_dim'
		}).css({
			'position' : 'fixed'
			, 'left' : 0
			, 'top' : 0
			, 'width' : '100%'
			, 'height' : '100%'
			, 'background' : '#000'
			, 'opacity' : 0.8
			, 'display' : 'none'
			, 'z-index' : '100'
		});
		
		// img 영역
		var $img_area_wrap = $('<div>', {
			'id' : 'NT_img_area_wrap'
		}).css({
			'position' : 'fixed'
			, 'left' : 0
			, 'top' : 0
			, 'width' : '100%'
			, 'height' : '100%'
			, 'display' : 'none'
			, 'z-index' : '110'
		});
		
		var $img_area = $('<div>', {
			'id' : 'NT_img_area'
		}).css({
			'position' : 'relative'
			, 'border' : '2px solid #fff'
			// , 'border-radius' : '10px'
			// , 'overflow' : 'hidden'
			, 'margin' : '0 auto'
			// , 'width' : '50%'
		});
		
		// 이미지 태그
		var $pop_img = $('<img>').css({
			'width' : '100%'
			, 'height' : '100%'
		});
		
		// 닫기 버튼
		var $close_btn = $('<a>', {
			'href' : '#'
		}).css({
			'position' : 'absolute'
			, 'right' : '-4px'
			, 'top' : '-36px'
			, 'display' : 'inline-block'
			, 'color' : '#fff'
			, 'font-size' : '40px'
			, 'font-family' : 'Arial'
			, 'padding' : '5px 0'
			, 'opacity' : 0.65
		}).html('×');
		
		// 공통 유틸
		var util = {
			// 이미지 보여줄 영역의 위치 셋팅
			setImgArea : function(){
		
				// 윈도우 높이, 넓이 
				var w_w = $(window).outerWidth();
				var w_h = $(window).outerHeight();
				
				// 설정된 이미지 고유 넓이, 높이
				var img_w = $pop_img.naturalWidth();
				var img_h = $pop_img.naturalHeight();
				
				// 설정된 최대 이미지 넓이, 높이보다 클경우 기본 최대 크기로 설정
				var set_w = (img_w > w_w * sets.view_percent / 100) ? w_w * sets.view_percent / 100 : img_w;
				var set_h = (img_h > w_h * sets.view_percent / 100) ? w_h * sets.view_percent / 100 : img_h;
				
				// 이미지 높이 설정
				var m_t = w_h / 2 - set_h / 2;
				
				$img_area.css({
					'width' : set_w
					, 'height' : set_h
					, 'margin-top' : m_t
				});
			}
			// 이미지 팝업 보여주기
			, showImg : function(){
				// 스크롤 막기
				// 서브 메뉴 보일시 window 스크롤은 하지 못하게 막는다.
				$('html').on({
					'mousewheel' : function(e){
						e.preventDefault();
					}
					, 'touchmove' : function(e){
						e.preventDefault();
					}
				});
				
				$img_dim.fadeIn();
				// 이미지 영역 셋팅
				util.setImgArea();
				$img_area_wrap.fadeIn();
			}
			// 이미지 팝업 숨기기
			, hideImg : function(){
				$('html').off('mousewheel').off('touchmove');
				$img_area_wrap.fadeOut();
				$img_dim.fadeOut();
				
			}
		};
		
		// 공통 이벤트
		$close_btn.on({
			'mouseenter' : function(){
				$(this).css('opacity', '1');
			}
			, 'mouseleave' : function(){
				$(this).css('opacity', '0.65');
			}
			, 'click' : function(e){
				e.preventDefault();
			
				// 이미지 팝업 닫는다.
				util.hideImg();
			}
		});
		
		// 이미지 팝업을 클릭시
		$img_area_wrap.click(function(){
			// 이미지 팝업 닫는다.
			util.hideImg();
		});
		
		// 윈도우 리사이즈시
		$(window).resize(function(){
			// 윈도우 넓이가 1024가 넘지 않을경우 이미지 팝업을 닫는다.
			if($(window).outerWidth() < 1024){
				util.hideImg();
			}
		});
		
		// dim 영역 추가
		$('body').append($img_dim);
		$img_area.append($pop_img);
		$img_area.append($close_btn);
		$img_area_wrap.append($img_area);
		// $img_area_wrap.append($close_btn);
		$('body').append($img_area_wrap);
		
		// 각각의 객체 return
		return this.each(function(){
			var $img = $(this);					// 보여줄 영역
			
			// img 클릭시
			$img.on({
				'click' : function(e){
					e.preventDefault();
					
					// 윈도우 넓이가 1024가 넘지 않을경우 새창으로 띄운다.
					if($(window).outerWidth() < 1024){
						var win = window.open($(this).attr('src'), 'win_img');
					}
					else {
						// 이미지 셋팅
						$pop_img.attr('src', $(this).attr('src'));
						util.showImg();
					}
				}
			});
		});
	};
	
	/*
	 * 배너 슬라이딩
	 * jquery, jquery-ui 기본적으로 있어야 한다.
	 * swipe_agree true일경우 jquery.event.move, jquery.event.swipe 가 있어야 한다.
	 * 
	 */
	$.fn.J_slide = function(options){
		var defaults = {
			l_navi : null				// 왼쪽 네비게이션
			, r_navi : null				// 오른쪽 네비게이션
			, p_navi : null				// 실행 네비게이션
			, navi_list : null			// 네비게이션 리스트
			, easing : 'linear'			// 애니메이션 형태
			, duration : 1000 			// 이동시간
			, hold_time : 3000			// 대기시간
			, type : 'slide-right'		// 슬라이드 형태
			, rotate : true				// 반복 여부
			, auto_start : true			// 자동 반복 여부
			, init_item : 0				// 최초 보여줄 아이템
			, swipe_agree : false		// 모바일기기등을 위해 swipe기능 활성 여부
		};
		
		// 기본값과 options값을 합침
		var sets = $.extend({}, defaults, options);
		
		// slide 에 필요한 값들
		var info = {
			isLog : true				// log 출력 여부
		};
		
		var util = {
			log : function(obj){
				if(info.isLog){
					console.log(obj);
				}
			}
			, error : function(obj){
				console.error(obj);
			}
		};
		
		// 적용된 객체가 여러개일경우 error 출력
		if($(this).length > 1){
			util.error('하나의 객체에만 사용해주세요! 정상적인 작동이 안됩니다.');
		}
		
		// swipe기능을 사용할경우
		if(sets.swipe_agree){
			util.log('swipe기능을 사용하기 위해 jquery.event.move, jquery.event.swipe 를 include 해주세요.');
		}
		
		// 각각의 객체 return
		return this.each(function(){
			var $view = $(this);					// 보여줄 영역
			var $wrap = $view.children('ul');		// 모든 아이템을 감싸는 영역
			var $items = $wrap.children('li');		// 아이템 리스트 객체
			var $l_navi = $(sets.l_navi);
			var $r_navi = $(sets.r_navi);
			var $p_navi = $(sets.p_navi);
			var $navies = $(sets.navi_list);
			var item_index = 0;						// 현재 아이템 위치 
			var item_size = $items.length;			// 아이템 총 개수
			var type = sets.type.split('-')[0];		// type
			var direct = sets.type.split('-')[1];	// 방향
			var show_type = 0;						// 기본 slide left, right : 0, slide up, down : 1, fade in : 2, fade out : 3
			
			var isAnimate = false;					// 애니메이션 여부
			var interval = null;					// interval 변수
			
			var method = {
				// 영역 설정
				setArea : function(){
					// item 넓이; 보이는 영역은 선, 마진이 들어갈수 있기 때문에 outer를 쓰지 않는다.
					$items.css({
						// 'height' : $view.height(),				// 높이는 response 적인 요소를 고려해 지정하지 않는다.
						'width' : $view.width()
					});
					
					var wrap_h = 0;
					var wrap_w = 0;
					// slide left, right 일때
					if(type == 'slide' && ( direct == 'left' || direct == 'right' )){
						$items.css('float' , 'left');
						
						// wrap 넓이, 높이 설정
						wrap_h = $view.height();
						wrap_w = $view.width() * item_size;
						
						show_type = 0;
					}
					
					// slide up, down 일때
					if(type == 'slide' && ( direct == 'up' || direct == 'down' )){
						// wrap 넓이, 높이 설정
						wrap_h = $view.height() * item_size;
						wrap_w = $view.width();
						
						show_type = 1;
					}
					
					// fade 일경우
					if(type == 'fade'){
						$items.css({'position' : 'absolute', 'top' : 0, 'left' : 0});
						
						if(direct == 'in'){ show_type = 2; }
						if(direct == 'out'){ show_type = 3; }
					}
					
					$wrap.css({
						'position' : 'relative', 
						'width' : wrap_w, 
						// 'height' : wrap_h			// 높이는 response 적인 요소를 고려해 지정하지 않는다.
					});
					
					$items.css('display', 'block');
				}
				// 바로 이동
				, set : function(index){
					var data = {'left' : 0, 'top' : 0};
					
					
					// slide left, right
					if(show_type == 0){
						data.left = -1 * $view.width() * index;
					}
					if(show_type == 1){
						data.top = -1 * $view.height() * index;
					}
					
					// wrap 위치 이동
					$wrap.css(data);
					
					// fade
					if(show_type == 2 || show_type == 3){
						$items.css('display', 'none').eq(item_index).css('display', 'block');
					}
					
					// 현재위치 셋팅
					item_index = index;
					// navi 셋팅
					method.setNavi();
				}
				// 아이템 이동
				, go : function(index){
					// 이동중이 아닐경우 실행
					if(!isAnimate){
						isAnimate = true;
						var data = {'left' : 0, 'top' : 0};
						
						// slide type
						if(type == 'slide'){
							// slide left, right
							if(show_type == 0){
								data.left = -1 * $view.width() * index;
							}
							if(show_type == 1){
								data.top = -1 * $view.height() * index;
							}
							
							// wrap 위치 이동
							$wrap.animate(data, {
								duration : sets.duration
								, easing : sets.easing
								, complete : function(){
									isAnimate = false;
									
									// 현재위치 셋팅
									item_index = index;
									// navi 셋팅
									method.setNavi();
								}
							});
						}
						
						// fade type
						if(type == 'fade'){
							var options = {
								'duration' : sets.duration
								, 'easing' : sets.easing
							};
							
							$items.fadeOut(options);
							
							options.complete = function(){
								isAnimate = false;
								// 현재위치 셋팅
								item_index = index;
								// navi 셋팅
								method.setNavi();
							};
							
							$items.eq(index).fadeIn(options);
						}
						
					}
				}
				// 이전
				, prev : function(){
					var index = item_index - 1;
					
					// 처음일경우 끝으로
					if(index == -1){ index = item_size - 1; }
					method.go(index);
				}
				// 이후
				, next : function(){
					var index = item_index + 1;
					
					// 끝일경우 처음으로
					if(index == item_size){ index = 0; }
					method.go(index);
				}
				// 재생
				, play : function(){
					$p_navi.addClass('on');
					interval = setInterval(function(){
						// left 나 up 일경우
						if(direct == 'left' || direct == 'up' || direct == 'in'){
							method.prev();
						}
						// right 나 down 일경우
						if(direct == 'right' || direct == 'down' || direct == 'out'){
							method.next();
						}
					}, sets.hold_time);
				}
				// 중지
				, stop : function(){
					$p_navi.removeClass('on');
					clearInterval(interval);
					interval = null;
				}
				// 네비 셋팅
				, setNavi : function(){
					// 리스트 on, off
					$navies.removeClass('on').eq(item_index).addClass('on');
					
					// 반복여부 따라서 이전,다음 버튼 on, off
					if(!sets.rotate){
						$l_navi.removeClass('off');
						$r_navi.removeClass('off');
							
						if(item_index == 0){ $l_navi.addClass('off'); }
						else if(item_index == item_size - 1){ $r_navi.addClass('off'); }
					}
				}
				// 화면 재설정
				, resize : function(){
					// 화면 크기 변경에 따른 아이템의 위치가 바뀌기 때문에 해당 위치로 계속 셋팅
					method.setArea();
					method.set(item_index);
				}
				, destroy : function(){
					// alert('a');
				}
			};
			
			// item 정렬
			method.setArea();
			
			// 위치 셋팅
			method.set(sets.init_item);
			
			// 자동 실행시
			if(sets.auto_start){
				method.play();
			}
			
			// navi event
			$l_navi.on({
				'click' : function(e){
					e.preventDefault();
					var auto = interval;
					
					// 자동재생을 일단 멈춤(재생중일때)
					if(auto != null){ method.stop(); }
					
					// 반복
					if(sets.rotate){
						method.prev();
					}
					// 미반복
					else {
						if(item_index > 0){ method.prev(); }
					}
					
					// 자동재생 시작
					if(auto != null){ setTimeout(method.play, sets.hold_time); }
				}
			});
			
			$r_navi.on({
				'click' : function(e){
					e.preventDefault();
					var auto = interval;
					
					// 자동재생을 일단 멈춤(재생중일때)
					if(auto != null){ method.stop(); }
					
					// 반복
					if(sets.rotate){
						method.next();
					}
					// 미반복
					else {
						if(item_index < item_size - 1){ method.next(); }
					}
					
					// 자동재생 시작
					if(auto != null){ setTimeout(method.play, sets.hold_time); }
				}
			});
			
			$p_navi.on({
				'click' : function(e){
					e.preventDefault();
					
					// 재생중
					if($(this).hasClass('on')){
						method.stop();
					}
					else {
						method.play();
					}
				}
			});
			
			$navies.on({
				'click' : function(e){
					e.preventDefault();
					// var auto = interval;
					
					// 자동재생을 일단 멈춤(재생중일때)
					// if(auto != null){ method.stop(); }
					
					var index = $navies.index(this);
					method.go(index);
					
					// 자동재생 시작
					// if(auto != null){ setTimeout(method.play, sets.hold_time); }
				}
			});
			
			// resize
			$(window).resize(function(){
				// 자동 움직임 막기
				// method.stop();
				method.resize();
			});
			
			// 현재 움직이고 있는지 아닌지 확인하기 위한 변수
			var played = false; 
			
			// 배너 이벤트 마우스 오버시 멈춘다.
			$view.on({
				'mouseenter' : function(){
					// interval 객체가 있을경우 플레이중
					if(interval != null){
						played = true;
						method.stop();
					}
				}
				, 'mouseleave' : function(){
					// 마우스 오버전에 자동재생중일경우만 다시 실행
					if(played){
						method.play();
					}
				}
			});
			
			$items.on({
				// drag 방지
				'dragstart' : function(e){
					e.preventDefault();
				}
			});
			
			// touch swipe 이용 이벤트
			if(sets.swipe_agree){
				$view.on({
					'swipeleft' : function(e){
						// 오른쪽으로 이동
						var auto = interval;
					
						// 자동재생을 일단 멈춤(재생중일때)
						if(auto != null){ method.stop(); }
						
						// 반복
						if(sets.rotate){
							method.next();
						}
						// 미반복
						else {
							if(item_index < item_size - 1){ method.next(); }
						}
						
						// 자동재생 시작
						if(auto != null){ setTimeout(method.play, sets.hold_time); }
					},
					'swiperight' : function(e){
						// 왼쪽으로 이동
						var auto = interval;
					
						// 자동재생을 일단 멈춤(재생중일때)
						if(auto != null){ method.stop(); }
						
						// 반복
						if(sets.rotate){
							method.prev();
						}
						// 미반복
						else {
							if(item_index > 0){ method.prev(); }
						}
						
						// 자동재생 시작
						if(auto != null){ setTimeout(method.play, sets.hold_time); }
					}
				});
			}
			
		});
	};
	
	/*
	 * 좌우 swipe 기능
	 * jquery 기본적으로 있어야 한다.
	 * 1개의 객체만 이용한다.
	 */
	$.fn.J_swipe = function(options){
		
		// 각각의 객체 return
		return this.each(function(){
			var _$view = $(this);					// 보여줄 영역
			var _$wrap = null;						// 모든 아이템을 감싸는 영역
			var _$items = _$view.children('div');	// 아이템 리스트 객체
			var _item_size = _$items.length;		// 아이템 총 개수
			
			var _isAnimate = false;					// 애니메이션 여부
			var _isTouch = false;					// 눌른상태인지 여부
			
			var defaults = {
				rotate : false,						// 반복유무
				threshold : 20,						// 이벤트 시작할 움직인 거리(%)
				start_index : 0,					// 최초 활성화할 item
				fixed_height : 0,					// 고정높이 영역
				moveEndFn : null,					// 움직이고 호출되는 함수
			};
			
			// 기본값과 options값을 합침
			var _sets = $.extend({}, defaults, options);
			
			
			var _initPoint = { x : 0, y : 0 };			// 처음 터치위치
			var _endPoint = { x : 0, y : 0 };			// 최종터치지점
			var _curPosition = '';						// 현재 아이템의 위치 
			
			var _curItem = _sets.start_index;
			var _fixedPosition = '';				// 고정유무(Y : 고정, N : 비고정)
			
			// touch 이벤트 존재시에는 touch 아닐시 마우스 이벤트
			var _isTouchSupported = 'ontouchstart' in window;
			var _startEv = _isTouchSupported ? 'touchstart' : 'mousedown';
			var _moveEv = _isTouchSupported ? 'touchmove' : 'mousemove';
			var _endEv = _isTouchSupported ? 'touchend' : 'mouseup';
			var _cancelEv = 'touchcancel';
			
			var _transformType = null;				// 변경 css 타입
			var _transformEvent = null; 				// transform 이벤트 타입
			
			var _dummyItem = null;					// 무한반복시 사용되는 임시 아이템
			
			var method = {
				init : function(){
					var bodyStyle = document.body.style;
					
					if(bodyStyle.OTransform !== undefined){ 
						_transformType = '-o-';
						_transformEvent = 'oTransitionEnd';
					}
					if(bodyStyle.MozTransform !== undefined){ 
						_transformType = '-moz-';
						_transformEvent = 'transitionend';
					}
					if(bodyStyle.webkitTransform !== undefined){ 
						_transformType = '-webkit-';
						_transformEvent = 'webkitTransitionEnd';
					}
					if(bodyStyle.msTransform !== undefined){ 
						_transformType = '-ms-';
						_transformEvent = 'msTransitionEnd';
					}
					
					_$view.css({
						'position' : 'relative',
						'height' : method.getViewHeight(),
					});
					
					var div = $('<div />', {
						'class' : 'slide-wrap'
					}).css({
						'position' : 'absolute',
						'width' : '100%',
						'height' : '100%',
					});
					
					_$items.wrapAll(div);
					
					_$wrap = _$view.children('.slide-wrap');
					
					method.setWrapPosition();
					
					// 아이템 정렬
					_$items.css({
						'position' : 'absolute',
						'width' : '100%',
						'top' : 0,
					})
					.addClass('slide-item')
					.each(function(index){
						$(this).css('left', (index * 100) + '%');
					});
					
					// 아이템 개수가 하나일경우는 적용하지 않는다.
					if(_item_size == 1){
						return;
					}
					
					method.addDummyItem();
					method.initEvent();
				},
				initEvent : function(){
					_$view.on(_startEv, method.touchStart);
					_$view.on(_moveEv, method.touchMove);
					_$view.on(_endEv, method.touchEnd);
					_$view.on(_cancelEv, method.touchEnd);
					_$wrap.on(_transformEvent, method.transitionEnd);
				},
				touchStart : function(e){
					var event = e.originalEvent;
					
					if(event.type == 'touchstart'){
						_initPoint.x = event.touches[0].clientX;
						_initPoint.y = event.touches[0].clientY;
					} else if('mousedown'){
						_initPoint.x = event.clientX;
						_initPoint.y = event.clientY;
					}
					
					_isTouch = true;
				},
				touchMove : function(e){
					var event = e.originalEvent;
					if(_isTouch){
						var x = 0;
						var c_x, c_y;
						if(event.type == 'touchmove'){
							_endPoint.x = event.touches[0].clientX;
							_endPoint.y = event.touches[0].clientY;
						} else if('mousemove'){
							_endPoint.x = event.clientX;
							_endPoint.y = event.clientY;
						}
						
						c_x = _endPoint.x - _initPoint.x;
						c_y = _endPoint.y - _initPoint.y;
						
						// 퍼센트로 변경
						c_x = c_x / _$view.outerWidth() * 100;
						
						// 최종 _$wrap 위치
						x = -1 * _curItem * 100 + c_x;
						
						var props = {};
						props[_transformType + 'transform'] = 'translate3d(' + x + '%, 0px, 0px)';
						
						// 좌우 스와이프 활성화
						if(_fixedPosition == 'Y'){
							// 화면 이동시킨다.
							_$wrap.css(props);
						}
						// 처음 스와이프 적용시
						if(_fixedPosition == ''){
							// 최초 두 좌표의 합이 5이상 움직이면 실행
							// else 문의 이벤트 방지때문에 일정 영역이 벗어날때 까지 이벤트를 차단해버리면 그후에 풀어버려도 윈도우 스크롤 이벤트가 먹지 않게 되어버림.
							// 적당한 움직임 5!
							if(Math.abs(c_x) + Math.abs(c_y) > 5){
								var angle = calculateAngle(_initPoint, _endPoint);
								
								// 스와이프가 해당 각도안에 있을경우 좌우 스와이프 실행
								if(angle >= 160 && angle <= 200 || angle <= 360 && angle >= 340 || angle <= 20){
									// 화면 이동시킨다.
									_$wrap.css(props);
									
									_fixedPosition = 'Y';
									// 윈도우 스크롤을 막기 위한 이벤트 방지
									e.preventDefault();
								} else {
									// 윈도우 스크롤 허용
									_fixedPosition = 'N';
								}
							} else {
								// 안드로이드 브라우저일경우 이벤트를 막지 않으면 move이벤트 한번만 호출(event를 계속 발생시키지 않고 처음 발생된것을 계속 리턴해준다.)
								// 크롬및 사파리등 모바일 브라우저는 계속 이벤트 호출해준다.
								// 안드로이드 브라우저일경우 천천히 움직일경우 5이상 움직이기도 전에 한번 호출 되고 더이상 이벤트가 발생하지 않기 때문에 아무런반응이 없기 때문에
								// 처음부터 이벤트를 막고 5이상이 되었을때 윈도우 스크롤을 풀어주거나 막는다.
								e.preventDefault();
							}
						}
					}
				},
				touchEnd : function(e){
					var event = e.originalEvent;
					
					// 좌우 고정일경우만 실행
					if(_fixedPosition == 'Y'){
						// 터치 엔드시에는 touch이벤트가 없기때문에 move에서 최종적으로 찍힌 포인트를 이용한다.
						// 원하는 만큼 swipe가 발생할 경우
						var distance = _initPoint.x - _endPoint.x;
						var direction = distance < 0 ? -1 : 1;
						
						if(Math.abs(distance) > _sets.threshold / 100 * $(window).outerWidth()){
							// 무한반복이 아닐경우
							if(!_sets.rotate && (direction == -1 && _curItem == 0 || direction == 1 && _curItem == _item_size - 1)){ method.go(_curItem); return; }
							method.go(_curItem + direction);
							
						} else {
							method.go(_curItem);
						}
					}
					
					_isTouch = false;
					_fixedPosition = '';
				},
				// 블럭 이동
				go : function(index){
					var props = {};
					var x = -1 * index * 100;
					props[_transformType + 'transform'] = 'translate3d(' + x + '%, 0px, 0px)';
					props[_transformType + 'transition'] = '200ms';
					// 화면 이동시킨다.
					_$wrap.css(props);
					
					if(_sets.rotate){
						if(index == -1){ index = _item_size - 1; }
						if(index == _item_size){	index = 0; }
					}
					
					_curItem = index;
				},
				// 속도가 있는 transition 이벤트 완료후 실행
				transitionEnd : function(){
					method.setWrapPosition();
					
					// 추가적인 item은 삭제한다.
					if(_dummyItem != null){ _dummyItem.remove(); }
					
					// 에니메이션(translate가 끝나고 현재 위치에 따라 더미 아이템 추가)
					method.addDummyItem();
					
					// view 높이 설정
					_$view.css('height', method.getViewHeight());
					
					// scrollTop으로
					$(window).scrollTop(0);
					
					if(typeof _sets.moveEndFn === 'function'){
						_sets.moveEndFn.call(this, _curItem);
					} else {
						var fn = window[_sets.moveEndFn];
						if(typeof fn === 'function'){
							_sets.moveEndFn.call(this, _curItem);
						}
					}
				},
				setWrapPosition : function(){
					// _$wrap에 걸린 에니메이션 속도를 초기화
					var props = {};
					props[_transformType + 'transition'] = '0ms';
					props[_transformType + 'transform'] = 'translate3d(' + (-1 * _curItem * 100) + '%, 0px, 0px)';
					
					// 화면 이동시킨다.
					_$wrap.css(props);
				},
				// 활성화된 item에따라 view의 높이 설정
				getViewHeight : function(){
					// view 높이 설정
					return h = $(window).outerHeight() - _sets.fixed_height > _$items.eq(_curItem).outerHeight() ? $(window).outerHeight() - _sets.fixed_height : _$items.eq(_curItem).outerHeight();
				},
				// 현재위치에 따라 더미 내용 붙인다.
				addDummyItem : function(){
					// 반복기능 적용시
					if(_sets.rotate){
						// 처음일경우 왼쪽에 끝내용 붙임
						if(_curItem == 0){
							_dummyItem = _$items.eq(_item_size - 1).clone();
							_dummyItem.css('left', '-100%');
							_$wrap.prepend(_dummyItem);
						}
						// 끝일경우 오른쪽에 처음내용 붙임
						if(_curItem == _item_size - 1){
							_dummyItem = _$items.eq(0).clone();
							_dummyItem.css('left', _item_size * 100 + '%');
							_$wrap.append(_dummyItem);
						}
					}
				},
//				removeEvent : function(ev){
//					_$view.off(ev);
//				},
			};
			
			method.init();
			
			// 해당위치로 이동
			$.fn.go = function(index){
				method.go(index);
			};
			
			/**
			* Calculate the angle of the swipe
			* @param {point} startPoint A point object containing x and y co-ordinates
		    * @param {point} endPoint A point object containing x and y co-ordinates
		    * @return int
			* @inner
			*/
			function calculateAngle(startPoint, endPoint) {
				var x = startPoint.x - endPoint.x;
				var y = endPoint.y - startPoint.y;
				var r = Math.atan2(y, x); //radians
				var angle = Math.round(r * 180 / Math.PI); //degrees

				//ensure value is positive
				if (angle < 0) {
					angle = 360 - Math.abs(angle);
				}

				return angle;
			}
		});
	};
	
	/*
	 * 테이블 리스트를 동적으로 밑으로 추가시켜서 보여준다.
	 * jquery 기본적으로 있어야 한다.
	 * 1개의 객체만 이용한다.
	 */
	$.fn.J_tableView = function(options){
		
		// 각각의 객체 return
		return this.each(function(){
			var $table = $(this);					// 해당 테이블
			var $wrap = null;						// 테이블을 감싸는 영역(스크롤이 될영역)
			var _dataList = options.dataList;		// 데이터 리스트
			var _defaults = {
				totCnt : 0,							// 총개수
				startIndex : 0,						// 시작 인덱스
				endIndex : 30,						// 끝 인덱스
				rowPerPage : 100,					// 추가 로드될 데이터수
				intervalTime : 300,					// 추가데이터 로드 시간
				callFunc : 'fnMakeList',			// 콜백함수
				wrapHeight : 348,					// 기본 wrap 최소 높이 
			};
			
			
			var _interval = null;					// interval 변수
			
			// 기본값과 options값을 합침
			var _sets = $.extend({}, _defaults, options);
			
			var _wrapHeight = _sets.wrapHeight;		// 데이터가 보일 영역 최소 높이
			
			var method = {
				init : function(){
					// 데이터가 들어갈 테이블을 스크롤할 영역으로 감싼다.
					$wrap = $('<div />', {
						'class' : 'tableScroll'
					}).css({
//						'overflow-y' : 'hidden',
						'height' : _wrapHeight,
					});
					
					$table.wrapAll($wrap);
					
					// wrap 최하단 위치 설정
					_wrapLastTop = $wrap.offset().top + _wrapHeight;
					
					method.addEvent();
				},
				addEvent : function(){
					
					// 총개시물수가 endIndex보다 작을경우
					if(_sets.endIndex > _sets.totCnt){
						_sets.endIndex = _sets.totCnt;
					}
					
					_interval = setInterval(method.callBack, _sets.intervalTime);
				},
				// 현재 보여줄 데이터 리턴
				callBack : function(){
					// 마지막 데이터인경우
					if(_sets.startIndex == _sets.totCnt){
						method.stop();
					}
					
					// 총 게시물이 0이 아니면서 마지막 데이터가 아닐경우만
					if(_sets.totCnt == 0 || _sets.startIndex != _sets.totCnt){
						var returnDataList = _dataList.slice(_sets.startIndex, _sets.endIndex);
						returnDataList = method.insertRownum(returnDataList);
						
						if(typeof _sets.callFunc === 'function'){
							_sets.callFunc.call(this, returnDataList);
						} else {
							var fn = window[_sets.callFunc];
							if(typeof fn === 'function'){
								fn(returnDataList);
							}
						}
						
						// 다음 넘겨줄 정보 셋팅
						method.pageSetting();
					}
				},
				// 데이터에 넘버링 넣음
				insertRownum : function(dataList){
					for(var i = 0; i < dataList.length; i++){
						dataList[i].ROWNUM = _sets.totCnt - _sets.startIndex - i;
					}
					return dataList;
				},
				// 다음 페이지 정보 셋팅
				pageSetting : function(){
					_sets.startIndex = _sets.endIndex;
					
					if(_sets.endIndex + _sets.rowPerPage >= _sets.totCnt){
						_sets.endIndex = _sets.totCnt;
					} else {
						_sets.endIndex += _sets.rowPerPage;
					}
				},
				// 데이터 갱신
				refresh : function(options){
					// 데이터 보이기 중지
					method.stop();
					
					// 기본값과 options값을 합침
					_dataList = options.dataList;
					_sets = $.extend({}, _defaults, options);
					
					// 데이터 보이기
					method.addEvent();
				},
				// 데이터 표시 중지
				stop : function(){
					if(_interval != null){
						clearInterval(_interval);
						_interval = null;
					}
				},
				// 데이터 삭제(remove 사용시 jquery remove시 remove함수를 호출해버림)
				dataRemove : function(){
					method.stop();
					// 테이블의 데이터 초기화
					$table.find('tr').remove();
				}
			};
			
			// 데이터 표시 시작
			$.fn.start = function(){
				method.init();
				method.callBack();
			};
			
			// 데이터 갱신
			$.fn.refresh = function(options){
				method.refresh(options);
			};
			
			// 데이터 삭제
			$.fn.dataRemove = function(){
				method.dataRemove();
			};
			
			// 데이터 표시 중지
			$.fn.stop = function(){
				method.stop();
			};
			
			// 스크롤 여부 확인
			$.fn.checkScroll = function(){
				return $wrap.outerHeight() < $table.outerHeight();
			};
		});
	};
	
})(jQuery, window, document);

jQuery.nl2br = function(varTest){
	return varTest.replace(/(\r\n|\n\r|\r|\n)/g, "<br>");
};