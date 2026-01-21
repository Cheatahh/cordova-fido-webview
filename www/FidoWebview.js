const exec = require('cordova/exec');

const FidoIntegration = {
    StatusCodes: {
        SUCCESS: 1, // payload: result
        FAILURE: 2, // payload: exception
        PROGRESS: 3  // payload: progress
    },
    getAssertion: function(clientData, userPin, onStatusChanged) {
        try {
            exec((result) => {
                onStatusChanged(result.statusCode, result.payload);
            }, (err) => {
                onStatusChanged(this.StatusCodes.FAILURE, err);
            }, 'FidoIntegration', 'getAssertion', [clientData, userPin]);
        } catch(err) {
            onStatusChanged(this.StatusCodes.FAILURE, err);
        }
    }
};

module.exports = FidoIntegration;