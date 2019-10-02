function isNumber(n) {
  let s = n.replace(/(\r\n|\n|\r|\s)/gm, '');
  let p = Number(s);
  if (isNaN(p) || s === '') {
    // console.log('not a number');
    return false;
  } else {
    // console.log('number', p);
    return true;
  }
}
