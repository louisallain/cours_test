import { StyleSheet } from "react-native";

export default StyleSheet.create({

    header: {
        backgroundColor: "#4285F4",
    },
    buttonContainer: {
        display: "flex",
        flexDirection: "column",
    },  
    logInButton: {
        backgroundColor: "#4285F4",
        marginLeft: 30,
        marginRight: 30,
        marginTop: 20,
        bottom: 0,
    },
    goToCreateAccountPageButton: {
        marginTop: 20,
        alignSelf: "flex-end",
        height: 20
    }
})