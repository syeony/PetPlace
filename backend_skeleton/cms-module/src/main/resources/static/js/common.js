/**
 * Copyright (c) 2000 by LG-EDS Systems Inc
 * All rights reserved.
 *
 * 자바스크립트 공통함수
 *
 * 주의: 아래의 모든 메소드는 입력폼의 필드이름(myform.myfield)을
 *       파라미터로 받는다. 필드의 값(myform.myfield.value)이 아님을
 *       유념할 것.
 *
 * @version 1.1, 2000/10/06
 * @author 박종진(JongJin Park), ecogeo@dreamwiz.com
 */

// 첨부파일 최대 개수

/**
 * 입력값이 NULL이면 공백 리턴
 */
//function nvl(obj) {
////	if (obj == null || obj == "null" || isNaN(obj)) {
//	if(!obj || isNaN(obj)){
//		
//		return "";
//	}
//	else{
//		return obj;
//	}
//}

function nvl(obj, val) {

    if (obj == null || obj == "null") {
//	if(!obj || isNaN(obj)){
        // 두번째 매개변수가 없을경우 빈값으로 설정
        return val === undefined ? "" : val;
    }
    else{
        return obj;
    }
}

/**
 * 입력값이 NULL인지 체크
 */
function isNull(input) {
    if (input.value == null || input.value == "") {
        return true;
    }
    return false;
}

/**
 * 입력값에 스페이스 이외의 의미있는 값이 있는지 체크
 */
function isEmpty(input) {
    if (input.value == null || input.value.replace(/ /gi,"") == "") {
        return true;
    }
    return false;
}

/**
 * 입력값에 특정 문자(chars)가 있는지 체크
 * 특정 문자를 허용하지 않으려 할 때 사용
 * ex) if (containsChars(form.name,"!,*&^%$#@~;")) {
 *         alert("이름 필드에는 특수 문자를 사용할 수 없습니다.");
 *     }
 */
function containsChars(input,chars) {
    for (var inx = 0; inx < input.value.length; inx++) {
        if (chars.indexOf(input.value.charAt(inx)) != -1)
            return true;
    }
    return false;
}

/**
 * 입력값에 특정 문자(chars)가 있는지 체크
 * 특정 문자를 허용하지 않으려 할 때 사용
 * ex) if (containsCharsSub(fileName,"!,*&^%$#@~;")) {
 *         alert("이름 필드에는 특수 문자를 사용할 수 없습니다.");
 *     }
 */
function containsCharsSub(input,chars) {
    for (var inx = 0; inx < input.length; inx++) {
        if (chars.indexOf(input.charAt(inx)) != -1)
            return true;
    }
    return false;
}

/**
 * 입력값이 특정 문자(chars)만으로 되어있는지 체크
 * 특정 문자만 허용하려 할 때 사용
 * ex) if (!containsCharsOnly(form.blood,"ABO")) {
 *         alert("혈액형 필드에는 A,B,O 문자만 사용할 수 있습니다.");
 *     }
 */
function containsCharsOnly(input,chars) {
    for (var inx = 0; inx < input.value.length; inx++) {
        if (chars.indexOf(input.value.charAt(inx)) == -1)
            return false;
    }
    return true;
}

/**
 * 입력값이 알파벳인지 체크
 * 아래 isAlphabet() 부터 isNumComma()까지의 메소드가
 * 자주 쓰이는 경우에는 var chars 변수를
 * global 변수로 선언하고 사용하도록 한다.
 * ex) var uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
 *     var lowercase = "abcdefghijklmnopqrstuvwxyz";
 *     var number    = "0123456789";
 *     function isAlphaNum(input) {
 *         var chars = uppercase + lowercase + number;
 *         return containsCharsOnly(input,chars);
 *     }
 */
function isAlphabet(input) {
    var chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    return containsCharsOnly(input,chars);
}

/**
 * 입력값이 알파벳 대문자인지 체크
 */
function isUpperCase(input) {
    var chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    return containsCharsOnly(input,chars);
}

/**
 * 입력값이 알파벳 소문자인지 체크
 */
function isLowerCase(input) {
    var chars = "abcdefghijklmnopqrstuvwxyz";
    return containsCharsOnly(input,chars);
}

/**
 * 입력값에 숫자만 있는지 체크
 */
function isNumber(input) {
    var chars = "0123456789";
    return containsCharsOnly(input,chars);
}

/**
 * 입력값이 알파벳,숫자로 되어있는지 체크
 */
function isAlphaNum(input) {
    var chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    return containsCharsOnly(input,chars);
}


/**
 * 입력값이 숫자,대시(-)로 되어있는지 체크
 */
function isNum(input) {
    var chars = "0123456789";
    return containsCharsOnly(input,chars);
}

/**
 * 입력값이 숫자,대시(-)로 되어있는지 체크
 */
function isNumDash(input) {
    var chars = "-0123456789";
    return containsCharsOnly(input,chars);
}



/**
 * 3자리 마다 콤마를 찍어준다
 */
function setComma(value) {
    var reg = /(^[+-]?\d+)(\d{3})/;
    var n = value;
    while(reg.test(n)) {
        n = n.replace(reg, '$1' + ',' + '$2');
    }
    return n;
}

function numberWithCommas(x) {
    return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

/**
 * yyyyMMdd 형식의 날짜를 yyyy+format+MM+format+dd 로 바꿔준다
 */
function setCommaDate(ymd, format){

    var rtnStr = "";
    rtnStr += ymd.substring(0,4) + format + ymd.substring(4,6) + format + ymd.substring(6,8);

    return rtnStr;
}

/**
 * yyyyMMddHHmm 형식의 날짜를 yyyy+format+MM+format+dd+''+HH+":"+mm 로 바꿔준다
 */
function setCommaDateTime(ymd, format){

    var rtnStr = "";
    rtnStr += ymd.substring(0,4) + format + ymd.substring(4,6) + format + ymd.substring(6,8) + " " + ymd.substring(8,10) + ":" + ymd.substring(10,12);

    return rtnStr;
}

/**
 * 입력값이 숫자,콤마(,)로 되어있는지 체크
 */
function isNumComma(input) {
    var chars = ",0123456789";
    return containsCharsOnly(input,chars);
}

/**
 * 입력값에서 콤마를 없앤다.
 */
function removeComma(input) {
    return input.value.replace(/,/gi,"");
}

/**
 * 입력값에서 콤마를 없앤다.
 */
function removeCommaDate() {
//    return $(".inputDate").val($(".inputDate").val().replace(/\./gi,""));
    $(".inputDate").each(function(){
        if($(this).val() != ''){
            $(this).val($(this).val().replace(/\./gi,""));
        }
    });
}

function addCommaDate() {
    $(".inputDate").each(function(){
        if($(this).val() != '' && $(this).val().length == 8){
            $(this).val($(this).val().substring(0, 4) + "." + $(this).val().substring(4, 6) +"."+ $(this).val().substring(6, 8));
        }
    });
}

/**
 * 입력값이 사용자가 정의한 포맷 형식인지 체크
 * 자세한 format 형식은 자바스크립트의 'regular expression'을 참조
 */
function isValidFormat(input,format) {
    if (input.value.search(format) != -1) {
        return true; //올바른 포맷 형식
    }
    return false;
}

/**
 * 입력값이 이메일 형식인지 체크
 */
function isValidEmail(input) {
//    var format = /^(\S+)@(\S+)\.([A-Za-z]+)$/;
    var format = /^((\w|[\-\.])+)@((\w|[\-\.])+)\.([A-Za-z]+)$/;
    return isValidFormat(input,format);
}

/**
 * 입력값이 전화번호 형식(숫자-숫자-숫자)인지 체크
 */
function isValidPhone(input) {
    var format = /^(\d+)-(\d+)-(\d+)$/;
    return isValidFormat(input,format);
}

/**
 * 선택된 라디오버튼이 있는지 체크
 */
function hasCheckedRadio(input) {
    if (input.length > 1) {
        for (var inx = 0; inx < input.length; inx++) {
            if (input[inx].checked) return true;
        }
    } else {
        if (input.checked) return true;
    }
    return false;
}


/**
 * 선택된 라디오버튼의 값을 리턴
 */
function hasCheckedRadioValue(input) {

    if (input.length > 1) {
        for (var inx = 0; inx < input.length; inx++) {
            if (input[inx].checked) return input[inx].value;
        }
    } else {
        if (input.checked) return input.value;
    }

    return '';
}

/**
 * 선택된 체크박스가 있는지 체크
 */
function hasCheckedBox(input) {
    return hasCheckedRadio(input);
}

/**
 * 입력값의 바이트 길이를 리턴
 * Author : Wonyoung Lee
 */
function getByteLength(input) {
    var byteLength = 0;
    for (var inx = 0; inx < input.value.length; inx++) {
        var oneChar = escape(input.value.charAt(inx));
        if ( oneChar.length == 1 ) {
            byteLength ++;
        } else if (oneChar.indexOf("%u") != -1) {
            byteLength += 2;
        } else if (oneChar.indexOf("%") != -1) {
            byteLength += oneChar.length/3;
        }
    }
    return byteLength;
}


function fnGetByte(strInput,ilimit){
    var i;
    var strLen;
    var strByte;
    var strRes = "";
    strLen = strInput.length;

    for(i=0, strByte=0; i<strLen; i++){
        var tmp = strInput.charAt(i);
        if(tmp >= ' ' && tmp <= '~' ){
            strByte++;
        }else{
            strByte += 2;
        }
        if(strByte > ilimit){
            break;
        }
        strRes += tmp;
    }
    var lenStr = fnCheckByte(strInput);
    if(Number(lenStr) > Number(ilimit)){
        strRes += "...";
    }
    return strRes;
}

function fnCheckByte(strInput){
    var i;
    var strLen;
    var strByte;
    strLen = strInput.length;


    for(i=0, strByte=0; i<strLen; i++){

        if(strInput.charAt(i) >= ' ' && strInput.charAt(i) <= '~' )
            strByte++;
        else
            strByte += 2;
    }
    return strByte;

}
function fnFileImg(fileName){
    var fileGif=new Array('bmp','doc','etc','exe', 'gif', 'gul','htm','html','hwp', 'ini','jpg', 'mgr', 'mpg', 'pdf', 'ppt', 'print', 'tif', 'txt', 'wav', 'xls', 'xml', 'zip');
    if(fileName == ''){
        return '';
    }

    var start = fileName.lastIndexOf(".");
    var name = '';
    if( start  > -1){
        name = fileName.substring(start+1).toLowerCase();
    }

    var retFlag = false;
    for(fileInx =0; fileInx< fileGif.length;fileInx++){
        if(name == fileGif[fileInx]){
            retFlag = true;
            break;
        }
    }
    var retStr = '';
    if(retFlag){
        retStr = '<img src="/zz/img/attach_'+name+'.gif" border="0">';
    }else{
        retStr = '<img src="/zz/img/icon_doc.gif"  border="0">';
    }
    return retStr;
}

function f_titleWidthHtml(str){
    var lang = fnCheckByte(str);
    document.write('<td width="'+((Number(lang)*6)+30)+'">'+str+'</td>');
}


function replaceRoman(val){
    var be_str = val;
    var aft_str = "";
    if(be_str == "1"){aft_str = "Ⅰ";}
    else if(be_str == "2"){aft_str = "Ⅱ";}
    else if(be_str == "3"){aft_str = "Ⅲ";}
    else if(be_str == "4"){aft_str = "Ⅳ";}
    else if(be_str == "5"){aft_str = "Ⅴ";}
    else if(be_str == "6"){aft_str = "Ⅵ";}
    else if(be_str == "7"){aft_str = "Ⅶ";}
    else if(be_str == "8"){aft_str = "Ⅷ";}
    else if(be_str == "9"){aft_str = "Ⅸ";}
    else if(be_str == "10"){aft_str = "Ⅹ";}
    else if(be_str == "11"){aft_str = "ⅩⅠ";}
    else if(be_str == "12"){aft_str = "ⅩⅡ";}
    else if(be_str == "13"){aft_str = "ⅩⅢ";}
    else if(be_str == "14"){aft_str = "ⅩⅣ";}
    else if(be_str == "15"){aft_str = "ⅩⅥ";}
    else if(be_str == "16"){aft_str = "ⅩⅦ";}
    else if(be_str == "17"){aft_str = "ⅩⅧ";}
    else if(be_str == "18"){aft_str = "ⅩⅧ";}
    else if(be_str == "19"){aft_str = "ⅩⅨ";}
    else if(be_str == "20"){aft_str = "ⅩⅩ";}
    else if(be_str == "21"){aft_str = "ⅩⅩⅠ";}
    else if(be_str == "22"){aft_str = "ⅩⅩⅡ";}
    else if(be_str == "23"){aft_str = "ⅩⅩⅢ";}
    else if(be_str == "24"){aft_str = "ⅩⅩⅣ";}
    else if(be_str == "25"){aft_str = "ⅩⅩⅥ";}
    else if(be_str == "26"){aft_str = "ⅩⅩⅦ";}
    else if(be_str == "27"){aft_str = "ⅩⅩⅧ";}
    else if(be_str == "28"){aft_str = "ⅩⅩⅧ";}
    else if(be_str == "29"){aft_str = "ⅩⅩⅨ";}
    else if(be_str == "30"){aft_str = "ⅩⅩⅩ";}
    return aft_str;
}


/**
 * @type   : function
 * @access : public
 * @desc   : input 필드나 textarea 의 사이즈를 제한하여 지정된 길이 이상
 *           입력할 경우 추가입력된 메세지를 출력하고 추가 입력된 문자는 삭제함
 *
 * <xmp>
 *  <INPUT TYPE="text" NAME="txtDesc" OnKeyUp="cfLengthCheck(this, 100);">
 *  <TEXTAREA name="txtDesc" rows="10" cols="60" OnKeyUp="cfLengthCheck(this, 4000);"></TEXTAREA>
 * </xmp>
 * @param  : targetObj - Textarea Object
 * @param  : maxLength - Max Length(영문기준)
 * @return :
 */
function cfLengthCheck(targetContext, targetObj, maxLength) {
    var len = 0;
    var newtext = "";
    for(var i=0 ; i < targetObj.value.length; i++) {
        var c = escape(targetObj.value.charAt(i));

        if ( c.length == 1 ) len ++;
        else if ( c.indexOf("%u") != -1 ) len += 2;
        else if ( c.indexOf("%") != -1 ) len += c.length/3;

        if( len <= maxLength ) newtext += unescape(c);
    }

    if ( len > maxLength ) {

        alert(targetContext+" "+maxLength+"자 이하로 넣어주십시오.");
        //cfAlertMsg(JMSG_COMF_ERR_007, [maxLength]);
        targetObj.value = newtext;
        targetObj.focus();
        return false;
    }
}


String.prototype.replaceAll = replaceAll;
function replaceAll( strValue1, strValue2){

    var strTemp = this;
    strTemp = strTemp.replace(new RegExp(strValue1,"g"), strValue2);
    return strTemp;
}

/**
 * @type   	: function
 * @access	: public
 * @desc	: 문자열 변경
 * @parameter str : 문자열 대체를 처리할 원 문자열
 * @parameter targetStr : 대체하기 원하는 문자(열) - 바꿀 문자
 * @parameter replaceStr : 대체될 문자(열) - 바뀌어질 문자Z
 */
function replaceAll1(str, searchStr, replaceStr) {
    return str.split(searchStr).join(replaceStr);
}

//form post make data (for ajax)
function f_formPost(formName) {

    var docForm = document.getElementById(formName);
    var strSubmitContent = '';
    var formElem;
    var strLastElemName = '';

    for (i = 0; i < docForm.elements.length; i++) {

        formElem = docForm.elements[i];
        switch (formElem.type) {
            case 'text':
            case 'number':
            case 'hidden':
            case 'password':
            case 'textarea':
            case 'select-one':
                if(i > 0){
                    strSubmitContent += "&"
                }
                //strSubmitContent += formElem.name + '=' + encodeURIComponent(formElem.value);
                strSubmitContent += formElem.name + '=' + encodeURIComponent(formElem.value);
                break;
            case 'radio':
                if (formElem.checked) {
                    if(i > 0){
                        strSubmitContent += "&"
                    }
                    strSubmitContent += formElem.name + '=' + encodeURIComponent(formElem.value);
                }
                break;
            case 'checkbox':
                if (formElem.checked) {
                    if(i > 0){
                        strSubmitContent += "&"
                    }
                    strSubmitContent += formElem.name + '=' + encodeURIComponent(formElem.value);
                }
                break;
        }
    }
    return strSubmitContent;
}

//form post make data (for ajax)
function f_rtnFormData(formName) {

    var docForm = document.getElementById(formName);
    var strSubmitContent = '';
    var formElem;
    var strLastElemName = '';

    for (i = 0; i < docForm.elements.length; i++) {

        formElem = docForm.elements[i];
        switch (formElem.type) {
            case 'text':
            case 'number':
            case 'hidden':
            case 'password':
            case 'textarea':
            case 'select-one':
                if(i > 0){
                    strSubmitContent += "^"
                }
                //strSubmitContent += formElem.name + '=' + encodeURIComponent(formElem.value);
                strSubmitContent += formElem.name + '=' + formElem.value;
                break;
            case 'radio':
                if (formElem.checked) {
                    if(i > 0){
                        strSubmitContent += "^"
                    }
                    strSubmitContent += formElem.name + '=' + formElem.value;
                }
                break;
            case 'checkbox':
                if (formElem.checked) {
                    if(i > 0){
                        strSubmitContent += "^"
                    }
                    strSubmitContent += formElem.name + '=' + formElem.value;
                }
                break;
        }
    }
    return strSubmitContent;
}


//파일 업로드시 제외되어야할 확장자들
function f_CheckExceptFileExt(objId){	//input id

    var exceptExtNames = "EXE,BAT,COM,JSP,ASP,HTML,PHP,SH,JS";	//제외될 파일 확장자
    var fileName = document.getElementsByName(objId);	//파일정보
    var checkMsg = "";

    for(i = 0; i < fileName.length; i++){

        if(fileName[i].value.length != 0){

            if(exceptExtNames.indexOf((fileName[i].value.substring(fileName[i].value.lastIndexOf(".")+1, fileName[i].value.length)).toUpperCase()) > 0){

                checkMsg = fileName[i].value.substring(fileName[i].value.lastIndexOf(".")+1, fileName[i].value.length) + " 확장자 파일은 첨부할 수 없습니다.";
                break;
            }
        }
    }

    return checkMsg;
}

//파일 업로드시 허용되는 확장자들
function f_CheckAcceptFileExt(objId, extList){	//input id

    var exceptExtNames = extList.toUpperCase().split('|');	//허용할 파일 확장자
    var fileName = document.getElementsByName(objId);	//파일정보

    for(var i = 0; i < fileName.length; i++){
        if(fileName[i].value.length != 0){
            var ext = fileName[i].value.substring(fileName[i].value.lastIndexOf(".")+1, fileName[i].value.length).toUpperCase();

            for(var j = 0; j < exceptExtNames.length; j++)
            {
                if(exceptExtNames[j] == ext){ return true; }
            }
        }
    }

    return false;
}

// 파일업로드시 허용되는 이미지크기들
// objName
function f_CheckImgSize(obj, width, heigth ){

    var file  = obj.files[0];
    var _URL = window.URL || window.webkitURL;
    var img = new Image();

    img.src = _URL.createObjectURL(file);
    img.onload = function() {

        if(img.width != width || img.height != heigth) {
            alert("이미지 가로 " + width + "px, 세로 " + heigth + "px로 맞춰서 올려주세요.");
            $(obj).val("");
        }
    }
}

//form make data (for back history)
function f_setFormData(formName, paramValue) {

    var docForm = document.getElementById(formName);

    var pramObjArray = paramValue.split("&");

    pramObjArray.sort();

    var pramTmp;
    var pramName;
    var pramValue;

    var objIdx = 0;
    var prevPramName

    for(i = 0; i < pramObjArray.length; i++){

        pramTmp = pramObjArray[i].split("=");
        pramName = pramTmp[0];
        pramValue =  pramTmp[1];

        if(prevPramName != pramName)
            objIdx = 0;
        else
            objIdx ++;

        if(eval("document.getElementsByName(\""+pramName+"\").length") == 1){	//객체가 1개일경우

            eval("docForm."+pramName).value = pramValue;
        }
        if(eval("document.getElementsByName(\""+pramName+"\").length") >= 1){	//객체가 1개이상일 경우


            if(eval("document.getElementsByName(\""+pramName+"\")[0]").type == "radio"
                || eval("document.getElementsByName(\""+pramName+"\")[0]").type == "checkbox"){

                for(k = 0; k < eval("document.getElementsByName(\""+pramName+"\")").length; k++){

                    if(eval("document.getElementsByName(\""+pramName+"\")")[k].value == pramValue){

                        eval("document.getElementsByName(\""+pramName+"\")")[k].checked = true;
                    }
                }
            }
            else{
                eval("document.getElementsByName(\""+pramName+"\")")[objIdx].value = pramValue;
            }
        }
        prevPramName = pramName;
    }
}

function f_getList(objId){

    var objList = document.getElementsByName(objId);
    return objList;
}

function f_getListChk(objId){
    var items = [];
    $('input[name="'+objId+'"]:checkbox:checked').each(function(){
        items.push($(this).val());
    });

    var tmp = items.join(',');
    return tmp;
}

//datepick 한글 셋팅
function settingdatepickerko(){

    $.datepicker.regional['ko']= {
        closeText:'닫기',
        prevText:'이전달',
        nextText:'다음달',
        currentText:'오늘',
        monthNames:['1월(JAN)','2월(FEB)','3월(MAR)','4월(APR)','5월(MAY)','6월(JUM)','7월(JUL)','8월(AUG)','9월(SEP)','10월(OCT)','11월(NOV)','12월(DEC)'],
        monthNamesShort:['1월','2월','3월','4월','5월','6월','7월','8월','9월','10월','11월','12월'],
        dayNames:['일','월','화','수','목','금','토'],
        dayNamesShort:['일','월','화','수','목','금','토'],
        dayNamesMin:['일','월','화','수','목','금','토'],
        weekHeader:'Wk',
        dateFormat:'yy-mm-dd',
        firstDay:0,
        isRTL:false,
        showMonthAfterYear:true,
        yearSuffix:''
    };

    $.datepicker.setDefaults($.datepicker.regional['ko']);
}

$(function(){

    //숫자만 입력 받도록 함.
    $('.numeric').css('imeMode','disabled').keypress(function(event) {

        if(event.which && (event.which != 46 && (event.which < 48 || event.which > 57)) ) {
            event.preventDefault();
        }
    }).keyup(function(){
        if( $(this).val() != null && $(this).val() != '' ) {
            $(this).val( $(this).val().replace(/[^0-9\.]/g, '') );
        }
    });

    // inputDate class 는 '.'으로 숫자 구분
    addCommaDate();

    /**
     * ajax 기본 호출시 셋팅
     */
    $.ajaxSetup({
        // 호출전에 request 해더에 'AJAX'를 추가하여 보낸다.
        beforeSend : function(xhr){
            xhr.setRequestHeader('AJAX', true);
        },
    });
});

//숫자만 입력 받도록 함.
function fnSetNumber(){

    $('.numeric').css('imeMode','disabled').keypress(function(event) {
        if(event.which && (event.which < 48 || event.which > 57) ) {
            event.preventDefault();
        }
    }).keyup(function(){
        if( $(this).val() != null && $(this).val() != '' ) {
            $(this).val( $(this).val().replace(/[^0-9]/g, '') );
        }
    });
}

function fnSetNumeric(){
    //숫자만 입력 받도록 함.
    $('.numeric').css('imeMode','disabled').keypress(function(event) {
        if(event.which && (event.which < 48 || event.which > 57) ) {
            event.preventDefault();
        }
    }).keyup(function(){
        if( $(this).val() != null && $(this).val() != '' ) {
            $(this).val( $(this).val().replace(/[^0-9]/g, '') );
        }
    });
}

function goHistoryBack(num){
    if(num == '' || num ==  null){ num = -1; }
    history.go(num);
}

function fnGoUrl(url){
    location.href = url;
}

function commify(n) {
    var reg = /(^[+-]?\d+)(\d{3})/;   // 정규식
    n += '';                          // 숫자를 문자열로 변환
    while (reg.test(n)) n = n.replace(reg, '$1' + ',' + '$2');
    return n;
}

//날짜 형식 비교(2월도 31일 까지 가능함)
function checkDateType(obj){
    var patt = /^\d{4}\.([0]\d|[1][0-2])\.([0-2]\d|[3][0-1])$/g;

    // 빈값이거나 해당 객체를 사용하지 않을경우는 제외
    if($(obj).val() != '' && !$(obj).prop('disabled') && !patt.test($(obj).val())){
        return false;
    }
    return true;
}

/*
 * code 관련 함수
 */

function getComboStr(resourceCodeList, valueColName, nameColName, selectedDtlCd, addOptionType){
    var rstlStr = '';
    var selStr = '';

    if (addOptionType == 'C') {	//Choice
        rstlStr = '<option value="">선택하세요</option>';
    }
    else if (addOptionType == 'A') {	//All
        rstlStr = '<option value="">전체</option>';
    }

    for(var i = 0; i < resourceCodeList.length; i++){
        selStr = "";
        var tmpMap = resourceCodeList[i];

        if(eval('tmpMap.' + valueColName) == selectedDtlCd)
            selStr = "selected='selected'";

        rstlStr += "<option value='"+eval('tmpMap.' + valueColName)+"' "+selStr+">"+eval('tmpMap.' + nameColName)+"</option>";
    }

    return rstlStr;
}

// 리스트에서 해당 코드 이름 조회
function getComboName(resourceCodeList, valueColName, nameColName, selectedDtlCd){
    var rstlStr = "";
    var selStr = "";

    for(var i = 0; i < resourceCodeList.length; i++){
        selStr = "";
        var tmpMap = resourceCodeList[i];
        if(eval('tmpMap.' + valueColName) == selectedDtlCd){
            rstlStr = eval('tmpMap.' + nameColName);
        }
    }

    return rstlStr;
}

///*
// * 브라우저 체크
// */
//function browser_check(){
//	var s = navigator.userAgent.toLowerCase();
//	var match = /(webkit)[ \/](\w.]+)/.exec(s) || /(opera)(?:.*version)?[ \/](\w.]+)/.exec(s) || /(msie) ([\w.]+)/.exec(s) || /(mozilla)(?:.*? rv:([\w.]+))?/.exec(s) || [];
//	return { name: match[1] || "", version: match[2] || "0" };
//}
//
///* 현재 URL clipboard 복사 */
//function clipBoardCopy(){
//	var url = document.location.href;
//	if(browser_check().name == 'msie'){
//		window.clipboardData.setData('Text', url);
//		alert('URL 주소가 복사되었습니다. 원하는 곳에 Ctrl+V로 붙여 넣기 하세요.');
//	} else {
//		prompt("이 글의 URL 주소입니다. Ctrl+C를 눌러 클립보드로 복사하세요", url);
//	}
//}

// 글자 크기 변경(true : 확대, false : 축소)
function fnChangFont(flag){
    // font 구하기
    var font_size = parseInt($('.contBox *').css('font-size'), 10);

    // 플러스
    if(flag){
        if(font_size < 14){
            $('.contBox *').css('font-size', font_size + 1);
//			$('.contBox *').each(function(){
//				var f = parseInt($(this).css('font-size'), 10);
//				if(f > 0){
//					$(this).css('font-size', f + 1);
//				}
//			});
        }
    } else {
        if(font_size > 12){
            $('.contBox *').css('font-size', font_size - 1);
//			$('.contBox *').each(function(){
//				var f = parseInt($(this).css('font-size'), 10);
//				if(f > 0){
//					$(this).css('font-size', f - 1);
//				}
//			});
        }
    }
}

/* 첨부파일 사이즈 필터링 함수 */
// 이 함수를 사용하고자 할때 상단에 jquery 및 jquery.form.min.js 가 import 되어있어야함
// 파일용량 체크 위해 임시로 등록
// ajax 파일업로드시에는 json형태로 리턴 받지 못한다.
// form 폼 이름 path 파일명을 제외한 절대경로 , size 파일의 최대 사이즈값(byte), callBack 콜백함수 
function saveFileValidate(form, path, size, callBack){

    var param = {'file_path' : path, 'max_size' : size};

    $(form).ajaxSubmit({
        url : '/common/file/saveFileAjax.do'
        , data : param
        , dataType : 'text'
        , success : function(response){
            var response = $.parseJSON(response.replace(/(<([^>]+)>)/ig,""));
            // error 코드시
            if(response.resultStats.resultCode == 'error'){
                alert(response.resultStats.resultMsg);
            }
            if(response.resultStats.resultCode == 'ok'){
                callBack.call(this, response.resultStats.doc_id);
            }
        }
        , error : function(){
            alert('저장에 실패하였습니다');
        }
    });
}


/**
 * @type   	: function
 * @access	: public
 * @desc	: string String::cut(int len) 글자를 앞에서부터 원하는 바이트만큼 잘라 리턴합니다. 한글의 경우 2바이트로 계산하며, 글자 중간에서 잘리지 않습니다.
 * @parameter len : 자를 글자 수
 * @parameter addStr : 추가될 문자열
 */
String.prototype.cut = function(len, addStr) {
    var str = this;
    var l = 0;
    for (var i=0; i<str.length; i++) {
        l += (str.charCodeAt(i) > 128) ? 2 : 1;
        if (l > len) return str.substring(0,i) + addStr;
    }
    return str;
}

/**
 * bool String::bytes(void)
 * 해당스트링의 바이트단위 길이를 리턴합니다. (기존의 length 속성은 2바이트 문자를 한글자로 간주합니다)
 */
String.prototype.bytes = function() {
    var str = this;
    var l = 0;
    for (var i=0; i<str.length; i++) l += (str.charCodeAt(i) > 128) ? 2 : 1;
    return l;
}

/*
 * 준비중 경고창
 */
//function fnReadyPage(){
//	alert('준비중입니다.');
//}


/**
 * 여러개의 첨부파일 양식을 그릴경우 같은 obj 안에서는 다시 그릴경우는 폼양식이 갱신이 되며
 * 새로운 곳은 sel_file_box[숫자], input_file_wrap[숫자] 이런식으로 증가하게 되며 file name인 upload만 초기는 숫자가 없고 그 이후로 upload[숫자] 형식으로 naming 이 된다.
 *
 * @param obj : 파일양식 그리영역
 * @param num : 현재 첨부파일 개수
 * @param max_count : 최대 첨부파일 개수
 */
function fnComboStrFile(obj, num, max_count){
    // 기본 값
    var ii = '0';
    // select박스 id, 파일박스 id
    var sel_id = 'sel_file_box';
    var file_box_id = 'input_file_wrap';

    var tag_n = 'upload';

    // 해당 obj영역에 첨부파일 양식이 있는지 확인
    // 양식이 있는경우
    if($(obj).find('.sel_file_box').length > 0){
        sel_id = $(obj).find('.sel_file_box').attr('id');
        file_box_id = $(obj).find('.input_file_wrap').attr('id');
        tag_n = $(obj).find('.input_file_wrap input[type=file]').attr('name');
    }
    // 없는경우
    else {
        // 총 첨부파일 양식의 숫자를 구한다.
        ii = $('.sel_file_box').length;

        // id 값에 숫자를 추가한다.
        sel_id += ii;
        file_box_id += ii;
        // 폼양식이 하나 이상일경우에만 upload에 숫자를 붙여준다.
        if(ii > 0){
            tag_n += ii;
        }
    }

    // 기존에 있는 이벤트를 제거한다.
    $('#' + sel_id).off('change');

    var mc = max_count ? max_count : MAX_FILE_COUNT;
    var n = mc - num;
    var html = '';

    // 첨부파일 최대개수일때
    if(mc == num){
        html += '<span class="no_file_text">더이상 등록하실수 없습니다.</span>';
    }
    else {
//		html += '<select id="' + sel_id + '" class="selectA sel_file_box">';
        html += '<select id="' + sel_id + '" class="form-control custom">';

        for(var i = 1; i <= n ; i++){
            html += '<option value="' + i + '">' + i + '</option>';
        }

        html += '</select>';
        html += '<div id="' + file_box_id + '" class="input_file_wrap">';
        html += '	<input type="file" name="' + tag_n + '" />';
        html += '</div>';
    }

    $(obj).html(html);

    // 첨부파일 개수 변경 이벤트
    $('#' + sel_id).on({
        'change' : function(e){
            var n = $(this).val();
            var txt = '';
            for(var i = 0; i < n; i++){
                txt += '<input type="file" name="' + tag_n + '" /><br/>';
            }

            $('#' + file_box_id).html(txt);
        }
    });
}

/**
 * 여러개의 첨부파일 양식을 그릴경우 같은 obj 안에서는 다시 그릴경우는 폼양식이 갱신이 되며
 * 새로운 곳은 sel_file_box[숫자], input_file_wrap[숫자] 이런식으로 증가하게 되며 file name인 upload만 초기는 숫자가 없고 그 이후로 upload[숫자] 형식으로 naming 이 된다.
 *
 * @param obj : 파일양식 그리영역
 * @param num : 현재 첨부파일 개수
 * @param max_count : 최대 첨부파일 개수
 */
function fnComboStrFile2(obj, num, max_count, name){
    // 기본 값
    var ii = name;
    // select박스 id, 파일박스 id, input file name
    var sel_id = 'sel_file_box';
    var file_box_id = 'input_file_wrap';
    var tag_n = 'upload_';

    // 총 첨부파일 양식의 숫자를 구한다.
    //ii = $('#sel_file_box').length;

    // id 값에 숫자를 추가한다.
    sel_id += ii;
    file_box_id += ii;
    tag_n += ii;

    // 기존에 있는 이벤트를 제거한다.
    $('#' + sel_id).off('change');

    var mc = max_count ? max_count : MAX_FILE_COUNT;
    var n = mc - num;
    var html = '';

    // 첨부파일 최대개수일때
    if(mc == num){
        html += '<span class="no_file_text">더이상 등록하실수 없습니다.</span>';
    }
    else {
//		html += '<select id="' + sel_id + '" class="selectA sel_file_box">';
        html += '<select id="' + sel_id + '" class="form-control custom">';

        for(var i = 1; i <= n ; i++){
            html += '<option value="' + i + '">' + i + '</option>';
        }

        html += '</select>';
        html += '<div id="' + file_box_id + '" class="input_file_wrap">';
        html += '	<input type="file" name="' + tag_n + '" />';
        html += '</div>';
    }

    $(obj).html(html);

    // 첨부파일 개수 변경 이벤트
    $('#' + sel_id).on({
        'change' : function(e){
            var n = $(this).val();
            var txt = '';
            for(var i = 0; i < n; i++){
                txt += '<input type="file" name="' + tag_n + '" /><br/>';
            }

            $('#' + file_box_id).html(txt);
        }
    });
}

/**
 * 웹쪽 첨부파일 폼
 * 여러개의 첨부파일 양식을 그릴경우 같은 obj 안에서는 다시 그릴경우는 폼양식이 갱신이 되며
 * 새로운 곳은 sel_file_box[숫자], input_file_wrap[숫자] 이런식으로 증가하게 되며 file name인 upload만 초기는 숫자가 없고 그 이후로 upload[숫자] 형식으로 naming 이 된다.
 *
 * @param obj : 파일양식 그리영역
 * @param num : 현재 첨부파일 개수
 * @param max_count : 최대 첨부파일 개수
 */
//function fnComboStrFileFront(obj, num, max_count){
//	// 기본 값
//	var ii = '0';
//	// select박스 id, 파일박스 id
//	var sel_id = 'sel_file_box';
//	var file_box_id = 'input_file_wrap';
//
//	var tag_n = 'upload';
//	
//	// 해당 obj영역에 첨부파일 양식이 있는지 확인
//	// 양식이 있는경우
//	if($(obj).find('.sel_file_box').length > 0){
//		sel_id = $(obj).find('.sel_file_box').attr('id');
//		file_box_id = $(obj).find('.input_file_wrap').attr('id');
//		tag_n = $(obj).find('.input_file_wrap input[type=file]').attr('name');
//	}
//	// 없는경우
//	else {
//		// 총 첨부파일 양식의 숫자를 구한다.
//		ii = $('.sel_file_box').length;
//		
//		// id 값에 숫자를 추가한다.
//		sel_id += ii;
//		file_box_id += ii;
//		// 폼양식이 하나 이상일경우에만 upload에 숫자를 붙여준다.
//		if(ii > 0){
//			tag_n += ii;
//		}
//	}
//	
//	// 기존에 있는 이벤트를 제거한다.
//	$('#' + sel_id).off('change');
//	
//	var mc = max_count ? max_count : MAX_FILE_COUNT;
//	var n = mc - num;
//	var html = '';
//	
//	// 첨부파일 최대개수일때
//	if(mc == num){
//		html += '<li><span class="no_file_text">더이상 등록하실수 없습니다.</span></li>';
//	}
//	else {
//		html += '<li><select id="' + sel_id + '" class="selectText sel_file_box" style="width:50px;">';
//		
//		for(var i = 1; i <= n ; i++){
//			html += '<option value="' + i + '">' + i + '</option>';
//		}
//		
//		html += '</select>최대 ' + mc + '개, 총 ' + MAX_SIZE_STR + ' 까지</li>';
//		
//		html += '<li><ul id="' + file_box_id + '" class="addfile">';
//		html += '	<li><input type="file" name="' + tag_n + '" class="inputFile" style="width:255px;" /></li>';
//		
//		html += '	</ul></li>';
//	}
//	
//	$(obj).html(html);
//	
//	// 첨부파일 개수 변경 이벤트
//	$('#' + sel_id).on({
//		'change' : function(e){
//			var n = $(this).val();
//			var txt = '';
//			for(var i = 0; i < n; i++){
//				txt += '<li><input type="file" name="' + tag_n + '" class="inputFile" style="width:255px;" /></li>';
//			}
//			
//			$('#' + file_box_id).html(txt);
//		}
//	});
//}

/**
 * ajax 호출 시 세션 끊겼을 경우 처리
 *
 * @param e
 * @param xhr
 * @param settings
 * @param exception
 */
//function ajaxJsonErrorAlert(jqXHR, textStatus, thrownError){
//	if(jqXHR.status	==0){ 						alert("네트워크를 체크해주세요.");
//	}else if(jqXHR.status	==404){ 			alert("페이지를 찾을수 없습니다.");
//	}else if(jqXHR.status	==500){ 			alert("서버에러가 발생하였습니다.");
//	}else if(textStatus	=='parsererror'){ 	alert("응답 메시지 분석에 실패 하였습니다.");
//	}else if(textStatus	=='timeout'){ 		alert("시간을 초과 하였습니다.");
//	}else {
//		alert('에러가 발생하였습니다.');
//	}
//}

// 정상적으로 json 호출이 되었지만 code error로 떨어질경우 해당 함수를 호출한다.
function ajaxErrorMsg(response){
    alert(response.resultStats.resultMsg);
}

// 시간을 더하거나 뺀다(시분만 이용[4자리])
function addTime(curTime, target, time){
    var h = parseInt(curTime.substring(0, 2), 10);
    var m = parseInt(curTime.substring(2, 4), 10);

    // 시간 계산
    if(target == 'H'){
        // 0 보다 작아질때
        if(h + time < 0){
            h = 24 + (h + time);
        } /*else if(h + time > 24){
			h = (h + time) - 24;
		} */else {
            h = h + time;
        }
    }

    // 분 계산
    if(target == 'M'){
        var m_m = time % 60;
        var m_h = parseInt(time / 60, 10);

        // 0 보다 작아질때
        if(m + m_m < 0){
            m = 60 + (m +  m_m);
            h = h + m_h - 1;
        } else if(m + m_m > 60){
            m = m + m_m - 60;
            h = h + m_h + 1;
        } else {
            m = m + m_m;
            h = h + m_h;
        }
    }

    var s_h = h < 10 ? '0' + h : '' + h;
    var s_m = m < 10 ? '0' + m : '' + m;

    return s_h + s_m;
}



Map = function(){
    this.map = new Object();
};
Map.prototype = {
    put : function(key, value){
        this.map[key] = value;
    },
    get : function(key){
        return this.map[key];
    },
    getNumber : function(key){
        return Number(this.map[key]);
    },
    containsKey : function(key){
        return key in this.map;
    },
    containsValue : function(value){
        for(var prop in this.map){
            if(this.map[prop] == value) return true;
        }
        return false;
    },
    isEmpty : function(key){
        return (this.size() == 0);
    },
    clear : function(){
        for(var prop in this.map){
            delete this.map[prop];
        }
    },
    remove : function(key){
        delete this.map[key];
    },
    keys : function(){
        var keys = new Array();
        for(var prop in this.map){
            keys.push(prop);
        }
        return keys;
    },
    values : function(){
        var values = new Array();
        for(var prop in this.map){
            values.push(this.map[prop]);
        }
        return values;
    },
    size : function(){
        var count = 0;
        for (var prop in this.map) {
            count++;
        }
        return count;
    }
};



$(function() {
    $(window).resize(function() {
        jQuery("#wrap").css('min-height', jQuery(window).height());
        jQuery(".sub_contents").css('min-height', jQuery(window).height()-164);

    });
    $(window).resize();
});


/* 탭 영역 */
var tab = $('.main_tab > li');
var con = $('.tab_content');

//클릭이벤트정의

tab.each(function(e) {

    $(this).click(function() {
        con.hide();
        tab.removeClass('on');
        con.eq(e).show();
        tab.eq(e).addClass('on');
    });

});

//초기설정
con.hide();
tab.eq(0).trigger('click');


/* 팝업이벤트 */
function toggle_object(post_id) {

    var obj = document.getElementById(post_id);

    if (obj.style == "" || obj.style.display == "block")
        obj.style.display = 'none';
    else

        obj.style.display = "block";
}

/* 로그인 & 비로그인시 슬라이드 메뉴 컨트롤 */
var obj = $(".m_gnb_header");

if($(obj).hasClass("login")){
    $(".gnb_user_name a").text("홍길동");
    $(".gnb_logout").show();
}else{
    $(".gnb_user_name a").text("로그인 하세요.");
    $(".gnb_logout").hide();
}

$(document).ready(function(){
    $( ".btn_search" ).click(function() {
        if($(".search_wrap").hasClass("on")) {
            $( ".search_wrap" ).removeClass( "on" );
        } else {
            $( ".search_wrap" ).addClass( "on" );
        }
    });
    $( ".s_inp" ).click(function() {
        $( ".search_list" ).addClass( "on" );
    });
    $( ".search_cancel" ).click(function() {
        $( ".search_wrap" ).removeClass( "on" );
    });
});

$(function(){
    /*스크롤 탑*/
    $("div.btn_top").fadeOut("slow");
    $("div.btn_pre").fadeOut("slow");

    $(window).scroll(function(){
        setTimeout(scroll_top, 1000);//화살표가 반응하여 생기는 시간
    });

    $(".btn_top").hover( function(){
        scroll_top();
    });

    $(".btn_pre").hover( function(){
        scroll_top();
    });

    $("#btn_top").click(function(){
        $("html, body").animate({ scrollTop: 0 }, 600);//화살표 클릭시 화면 스크롤 속도
        return false;
    });

    //



})

/*스크롤 탑*/
function scroll_top(){
    if($(window).scrollTop()<=1) {
        $("#btn_top").fadeOut("slow");
        $("#btn_pre").fadeOut("slow");
    } else {
        $("#btn_top").fadeIn("slow");
        $("#btn_pre").fadeIn("slow");
    }
}


/**
 * @type   	: function
 * @access	: public
 * @desc	: 메타테그 insert 메소드
 * @parameter servie : sns servie(facebook, twitter, nate)
 * @parameter title : article title, cotent : article content
 * @parameter cotent : article content
 * @parameter imgUrl : image Url
 * @parameter pub_time : article published time
 */
function fnMetaTag_add( service, title, content, img_url, pub_time ){

    var ins_tag_str 	= '';
    var link_url			= location.href;
    var site_name	= '글로벌뉴스 경제 모바일';

    if( service == "facebook" ){
//		<meta property="fb:app_id" content="앱아이디" />      앱아이디
        ins_tag_str = '<meta property="og:title" content="'+ title +'" />';													// 웹 사이트의 제목
        ins_tag_str += '<meta property="og:type" content="article" />';													// 사이트 종류
        ins_tag_str += '<meta property="og:image" content="' + img_url + '" />';										// 대표 이미지 URL (이미지를 여러 개 지정할 수 있음)
        ins_tag_str += '<meta property="og:site_name" content="' + site_name + '" />';							// 웹 사이트의 제목
        ins_tag_str += '<meta property="og:url" content="' + link_url + '" />';											// 표시하고싶은URL
        ins_tag_str += '<meta property="og:description" content="' + content + '" />';								// 페이지 설명
        ins_tag_str += '<meta property="article:published_time" content="' + pub_time + '" />';
    }else if( service == "twitter" ){
        ins_tag_str = '<meta name="twitter:card" content="summary_large_image" />';								// 트위터 카드 summary는 웹페이지에 대한 요약정보를 보여주는 카드로 우측에 썸네일을 보여주고 그 옆에 페이지의 제목과 요약 내용을 보여준다.
        ins_tag_str += '<meta name="twitter:site" content="' + site_name + '" id="MetaTwitterSite" />';		// 트위터 카드에 사이트 배포자 트위터아이디
        ins_tag_str += '<meta name="twitter:creator" content="' + site_name + '" />';								// 트위터 카드에 배포자 트위터아이디
        ins_tag_str += '<meta name="twitter:url" content="' + link_url + '" />';										// 트위터 카드를 사용하는 표시하고싶은URL
        ins_tag_str += '<meta name="twitter:title" content="' + title + '" />';											// 트위터 카드에 나타날 웹 사이트의 제목
        ins_tag_str += '<meta name="twitter:image" content="' + img_url + '" id="MetaTwitterImage" />';	// 트위터 카드에 보여줄 대표 이미지 URL
        ins_tag_str += '<meta name="twitter:description" content="' + content + '" />';							// 트위터 카드에 나타날 요약 설명
    }else if( service == "nate" ){
        ins_tag_str = '<meta name="nate:title" content="'+ title +'" />';													// 웹 사이트의 제목
        ins_tag_str += '<meta name="nate:description" content="' + content + '" />';								// 페이지 설명
        ins_tag_str += '<meta name="nate:site_name" content="' + site_name + '" />';							// 웹 사이트의 제목
        ins_tag_str += '<meta name="nate:url" content="' + link_url + '" />';											// 표시하고싶은URL
        ins_tag_str += '<meta name="nate:image" content="' + img_url + '" />';										// 대표 이미지 URL
    }

    $("meta:last").after(ins_tag_str);
}


/**
 * @type   	: function
 * @access	: public
 * @desc	: 유닉스 타임스탬프
 * @parameter regiDate : 날짜-시간 ( ex) 2016/01/06 13:05:06 )
 */
function fn_fetch_unix_timestamp( regiDate ){
    var timestamp = new Date(regiDate);
    return Math.floor( timestamp.getTime() / 1000);
}

/**
 * @type   	: function
 * @access	: public
 * @desc	: ", ' 문자열 삭제
 * @param str
 * @returns
 */
function fn_del_Entity( str ){
    return str.replace(/"/g, "").replace(/'/g, "");
}

//3자리 마다  ',' 넣어주기
function numberFormatter(obj){
    var num = obj.value.replace(/[^0-9]/g, '');
    obj.value = setComma(num);
}


/**
 * 오늘날짜를 yyyyMMdd 형식으로 가져오기
 */
function getDate()
{
    var date = new Date();

    var year = date.getFullYear();
    var month = '' + (date.getMonth() + 1);
    var day = '' + date.getDate();

    if(month.length == 1)
    {
        month = '0' + month;
    }

    if(day.length == 1)
    {
        day = '0' + day;
    }

    var fullDay = year + '' + month + '' + day;

    return fullDay;
}

/*숫자 이외 문자 제거*/
function specialCharRemove(obj){
    var val = "";
    var pattern = /[^(0-9)]/gi;

    if(pattern.test(obj)){
        val = obj.replace(pattern,"");
    }
    return val;
}

/**
 * 오늘날짜를 yyyyMMdd 형식으로 가져오기
 */
function getDateToday(str)
{
    var date = new Date();

    var year = date.getFullYear();
    var month = '' + (date.getMonth() + 1);
    var day = '' + date.getDate();
    var hours = '' + date.getHours();
    var minutes = '' + date.getMinutes();
    var seconds = '' + date.getSeconds();

    if(month.length == 1)
    {
        month = '0' + month;
    }

    if(day.length == 1)
    {
        day = '0' + day;
    }

    if(hours.length == 1)
    {
        hours = '0' + hours;
    }

    if(minutes.length == 1)
    {
        minutes = '0' + minutes;
    }

    if(seconds.length == 1)
    {
        seconds = '0' + seconds;
    }

    var fullDay = year + str + month + str + day + ' ' + hours + ':' + minutes + ':' + seconds;

    return fullDay;
}

/**
 *
 * (1) form에 해당하는 name의 input 이 있으면, 생성하지 않고 값을 넣어주기
 * (2) form에 해당하는 name의 input 이 없으면, input hidden을 만들고 값 넣어서 form에 추가하기
 * @param fNm
 * @param nm
 * @param value
 */
function fnAddElement(fNm, nm, value){

    var input = document.createElement("input");
    input.setAttribute('type', 'hidden');
    input.setAttribute('name', nm);
    input.setAttribute('value', value);
    $("#" + fNm).append(input);
}

/**
 * 목록화면에서 페이지네비게이션을 통해 페이지 이동.
 * @param currentPage
 */
function fnGoPage(currentPage){
    $("#currentPage").val(currentPage);
    $("#aform").submit();
}

/**
 * 목록화면에서 검색기능
 */
function fnSearch(){
    $("#aform").submit();
}

/**
 * 로그아웃
 * Spring Security 를 이용해 로그아웃
 */
function fnLogOut(){
    $("#logout").submit();
}

function fnGetUrl(){
    var url = $(location).attr('pathname');
    return url;
}