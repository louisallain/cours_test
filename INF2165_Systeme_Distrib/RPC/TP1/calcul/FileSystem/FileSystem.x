const MAX = 1000;
typedef char FileIdentifier[MAX];
typedef int FilePointer;
typedef int Length;

struct Data {
    int length;
    char buffer[MAX];
};

struct writeargs {
    FileIdentifier filename;
    FilePointer position;
    Data data;
};

struct readargs {
    FileIdentifier filename;
    FilePointer position;
    Length length;
};

struct deleteargs {
    FileIdentifier filename;
}

program FILEREADWRITE {
    version VERSION {
        void WRITE(writeargs) = 1;
        Data READ(readargs) = 2;
        void DELETE(deleteargs) = 3;
    } = 2;
} = 9999;