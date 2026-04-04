import { Amplify } from 'aws-amplify';

Amplify.configure({
    Auth: {
        Cognito: {
            userPoolId: 'us-east-1_lEeDIOvEb',
            userPoolClientId: '3ru9v2d905sdq0gamndmh0uprq',
            loginWith: {
                email: true,
            }
        }
    }
});

export default Amplify;