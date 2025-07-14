var EXCEPT_EXT = 'exe,jsp,java,class,bat,sh,com,asp,php';
// 20M ( 1024 * 1024 * 20)
// 일반 첨부파일
var MAX_SIZE = '2097152';
var MAX_SIZE_STR = '2MB';
// 이미지 첨부파일
var MAX_SIZE_IMG = '2097152';
var MAX_SIZE_IMG_STR = '2MB';

var MAX_FILE_COUNT = 10;
/**
 * @author Administrator
 */

$.validator.addMethod("checkHangleUserName",function(value){
	return /^[\uAC00-\uD7A3]+$/.test(value);
},"한글로만 입력해 주십시오.");

//영문시작 영문숫자 체크
$.validator.addMethod("checkIDPW",function(value){
	return /^[a-zA-Z]{1}[a-zA-Z0-9]+$/.test(value);
},"영문시작 + 영문숫자만 입력해 주십시오.");

//전화번호 체크
$.validator.addMethod("checkPhone",function(value){
	return /^([0-9]{2}|[0-9]{3})\-([0-9]{3}|[0-9]{4})\-([0-9]{4})+$/.test(value);
},"전화번호 양식에 맞게 넣어주십시오.");

//휴대전화번호 체크
$.validator.addMethod("checkMobile",function(value){
	return /^(0)([0-9]{2})\-([0-9]{3}|[0-9]{4})\-([0-9]{4})+$/.test(value);
},"휴대전화번호 양식에 맞게 넣어주십시오.");

//날짜체크
$.validator.addMethod("checkDate",function(value){
	return /^([0-9]{4})\-([0-9]{2})\-([0-9]{2})+$/.test(value);
},"날짜양식에 맞게 넣어주십시오.");

//select 선택
$.validator.addMethod("checkSelect",function(value){
	return value == '' ? false : true;
},"값을 선택해주세요");

// 숫자만 입력
$.validator.addMethod("checkNum",function(value){
	return /^[0-9]*$/.test(value.replace(/,/g, ""));
},"숫자만 넣어주세요");

// 특수문자 제외 입력
$.validator.addMethod("checkHanEngNum",function(value){
	return /^[a-zA-Z0-9\uAC00-\uD7A3]+$/.test(value);
},"한글, 영문, 숫자만 넣어주세요.");

//특수문자 제외 입력
$.validator.addMethod("checkEngNum",function(value){
	// 값이 없을경우 true
	if(value.replace(/ /gi, '') == ''){ return true; }
	return /^[a-zA-Z0-9]+$/.test(value);
},"영문, 숫자만 넣어주세요.");

// input file[name=param] 인것을 가지고 validate 처리
// param 으로 name을 넣는것은 여러개의 태그 체크를 하지 못하여서 별로도 만들어서 체크하도록 함
$.validator.addMethod("checkExt",function(value, el, param){
	var result = true;
	var name = $(el).attr('name');
	// input type이 file이고 name은 param 파일이 선택되어진것만 체크
	var $files = $('input[type=file][name=' + name + ']').filter(function(i){
		return $(this).val() != '' ? true : false;
	});
	
	$files.each(function(index){
		var i = $(this).val().lastIndexOf('.');
		var ext = $(this).val().substring(i + 1, $(this).val().length).toLowerCase();
		
		// 허용 확장자 확인
		if(EXCEPT_EXT.indexOf(ext) > -1 && ext != ''){ 
			result = false;
			return;
		} else {
			result = true;
		}
	});
	
	return result;
	
},"해당 확장자는 올릴수 없습니다");

// 총 파일 사이즈 계산
// param(name|size) 형식으로 
// input file[name=param.name] 인것을 가지고 validate 처리
$.validator.addMethod('filesize', function(value, el, param){
	var result = true;
	
	var name = $(el).attr('name');
	var size = param;
	
	// input type이 file 인걸 찾는다.
	var $files = $('input[type=file][name=' + name + ']').filter(function(i){
		return $(this).val() != '' ? true : false;
	});
	
	var fs = 0;
	
	$files.each(function(index){
		var file = $(this)[0];
		// 파일 사이즈 체크
		try {
			// 파일 크기 합산
			fs += file.files[0].size;
			
			if(fs > size){
				result = false;
				return;
			}
		} catch(err){
			// ie 9 이하는 file.size 를 찾지 못하고 error가 남(file api가 없기 때문에)
			// 그래서 일단 사이즈 체크 하지 않고 true로 넘기고 서버단에서 별도 작업을 통해 사이즈 체크를 해야함
			result = true;
			return;
		}
	});
	
	return result;
});

// 각각 파일 사이즈
//param(name|size) 형식으로 
//input file[name=param.name] 인것을 가지고 validate 처리
$.validator.addMethod('eachfilesize', function(value, el, param){
	var result = true;
	
	var name = $(el).attr('name');
	var size = param;
	
	// input type이 file 인걸 찾는다.
	var $files = $('input[type=file][name=' + name + ']').filter(function(i){
		return $(this).val() != '' ? true : false;
	});
	
	$files.each(function(index){
		var file = $(this)[0];
		
		// 파일 사이즈 체크
		try {
			if(file.files[0].size > size){
				result = false;
				return;
			}
		} catch(err){
			// ie 9 이하는 file.size 를 찾지 못하고 error가 남(file api가 없기 때문에)
			// 그래서 일단 사이즈 체크 하지 않고 true로 넘기고 서버단에서 별도 작업을 통해 사이즈 체크를 해야함
			result = true;
			return;
		}
	});
	
	return result;
});

$.validator.addMethod("checkDup",function(value, param){
	// 해당 input name 을 가져온다.
	var name = $(param).attr('name');
	
	// 같은 name을 가지고 있는 input 태그중에 자신과 같은 값을 검색
	var len = $('[name=' + name + ']').filter(function(){
		return $(this).val() == value ? true : false;
	}).length;
	
	// 자신과 같은 값이 있는경우
	if(len > 1){ return false; }
	return true;
	
},"중복된 값이 있습니다.");