function isNumber(n) {
  // Remove spaces, newlines, carriage returns, 
  let s = n.replace(/(\r\n|\n|\r|\t|\s)/gm, '');
  // Construct a number
  let p = Number(s);
  // Check if NaN or empty string (which would eval to 0) and return appropriately
  if (isNaN(p) || s === '') {
    // console.log('not a number');
    return false;
  } else {
    // console.log('number', p);
    return true;
  }
}
