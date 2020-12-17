import { StyleSheet, Dimensions } from "react-native";

export default StyleSheet.create({

    container: {
        display: "flex",
        justifyContent: "center",
        alignContent: "center",
        alignItems: "center"
    },
    unlockButton: {
        borderRadius: Dimensions.get("screen").width * 0.4,
        width: Dimensions.get("screen").width * 0.8,
        height:  0,
        paddingTop: Dimensions.get("screen").width * 0.4,
        paddingBottom: Dimensions.get("screen").width * 0.4,
        justifyContent: "center",
        marginLeft: "auto",
        marginRight: "auto",
    },
    loading: {
        position: "absolute",
        elevation: 10,
    }
})