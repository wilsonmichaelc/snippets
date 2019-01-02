// Convert camelCase string to sentence with first letter uppercased
convertCamelCaseToSentence(camelCase) {
    const sentence = [];
    let word = '';
    for (let i = 0; i < camelCase.length; i++) {
        const c = camelCase.charAt(i);
        if (c === c.toLowerCase()) {
            word = word + c;
        } else {
            sentence.push(word.charAt(0).toUpperCase() + word.slice(1));
            word = c;
        }
    };
    sentence.push(word);
    return sentence.join(' ');
}
